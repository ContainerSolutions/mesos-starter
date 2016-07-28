package com.containersolutions.mesos.scheduler.state;

import com.containersolutions.mesos.scheduler.TaskDescription;
import com.containersolutions.mesos.scheduler.events.FrameworkRegistreredEvent;
import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.apache.mesos.state.State;
import org.apache.mesos.state.ZooKeeperState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.util.SerializationUtils;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static com.containersolutions.mesos.utils.MesosHelper.isTerminalTaskState;

public class StateRepositoryZookeeper implements StateRepository {
    protected final Log logger = LogFactory.getLog(getClass());
    AtomicReference<Protos.FrameworkID> frameworkId = new AtomicReference<>();

    State zkState;

    @Autowired
    Environment environment;

    @Value("${mesos.framework.name:default}")
    String frameworkName;

    @Override
    public Optional<Protos.FrameworkID> getFrameworkID() {
        byte[] value;
        try {
            value = zkState.fetch("frameworkid").get().value();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to fetch framework id from Zookeeper");
        }
        if (value.length == 0) {
            return Optional.empty();
        }
        return Optional.of(Protos.FrameworkID.newBuilder().setValue(((String) SerializationUtils.deserialize(value))).build());
    }

    public void connect() {
        zkState = new ZooKeeperState(
                environment.getRequiredProperty("mesos.zookeeper.server"),
                1000,
                TimeUnit.MILLISECONDS,
                "/" + environment.getProperty("mesos.framework.name", "default")
        );
    }

    @EventListener
    public void onFrameworkRegistered(FrameworkRegistreredEvent event) {
        logger.debug("Received frameworkId=" + event.getFrameworkID().getValue());
        frameworkId.set(event.getFrameworkID());
        set("frameworkid", frameworkId.get().getValue());
    }

    @EventListener
    public void onStatusUpdate(StatusUpdateEvent event) {
        if (isTerminalTaskState(event.getTaskStatus().getState())) {
            set("tasks",
                    allTaskDescriptions().stream()
                            .filter(task -> !task.getTaskId().equals(event.getTaskStatus().getTaskId().getValue()))
                            .collect(Collectors.toSet())
            );
        }
    }

    @Override
    public void store(TaskDescription taskDescription) {
//        logger.debug("Persisting taskInfo for taskId=" + taskInfo.getTaskId().getValue());
        Set<TaskDescription> taskInfos = allTaskDescriptions();
        taskInfos.add(taskDescription);
        set("tasks", taskInfos);

    }

    @Override
    public Set<TaskDescription> allTaskDescriptions() {
        try {
            byte[] existingNodes = zkState.fetch("tasks").get().value();
            if (existingNodes.length == 0) {
                return new HashSet<>();
            }
            return (Set<TaskDescription>) SerializationUtils.deserialize(existingNodes);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Failed to get state from Zookeeper", e);
        }

    }

    private void set(String key, Object value) {
        try {
            zkState.store(zkState.fetch(key).get().mutate(SerializationUtils.serialize(value))).get();
        } catch (InterruptedException | ExecutionException  e) {
            throw new RuntimeException("Unable to set zNode", e);
        }
    }

    @PreDestroy
    public void cleanup() throws ExecutionException, InterruptedException {
        zkState.names().get().forEachRemaining(name -> {
            try {
                zkState.expunge(zkState.fetch(name).get()).get();
            } catch (InterruptedException | ExecutionException e) {
                logger.warn("Failed to delete key " + name);
            }
        });
    }
}
