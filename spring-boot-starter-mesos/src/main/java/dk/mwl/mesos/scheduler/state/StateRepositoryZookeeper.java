package dk.mwl.mesos.scheduler.state;

import dk.mwl.mesos.scheduler.events.FrameworkRegistreredEvent;
import dk.mwl.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.apache.mesos.state.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.util.SerializationUtils;

import javax.annotation.PreDestroy;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static dk.mwl.mesos.utils.MesosHelper.isTerminalTaskState;

public class StateRepositoryZookeeper implements StateRepository {
    protected final Log logger = LogFactory.getLog(getClass());
    AtomicReference<Protos.FrameworkID> frameworkId = new AtomicReference<>();

    @Autowired
    State zkState;

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
        return Optional.of(Protos.FrameworkID.newBuilder().setValue(new String(value)).build());
    }

    @EventListener
    public void onFrameworkRegistered(FrameworkRegistreredEvent event) {
        logger.debug("Received frameworkId=" + event.getFrameworkID().getValue());
        frameworkId.set(event.getFrameworkID());
        set("frameworkid", frameworkId.get());
    }

    @EventListener
    public void onStatusUpdate(StatusUpdateEvent event) {
        if (isTerminalTaskState(event.getTaskStatus().getState())) {
            set("tasks",
                    allTaskInfos().stream()
                            .filter(task -> !task.getTaskId().equals(event.getTaskStatus().getTaskId()))
                            .collect(Collectors.toSet())
            );
        }
    }

    @Override
    public void store(Protos.TaskInfo taskInfo) {
        Set<Protos.TaskInfo> taskInfos = allTaskInfos();
        taskInfos.add(taskInfo);
        set("tasks", taskInfos);
    }

    @Override
    public Set<Protos.TaskInfo> allTaskInfos() {
        try {
            byte[] existingNodes = zkState.fetch("tasks").get().value();
            if (existingNodes.length == 0) {
                return new HashSet<>();
            }
            return (Set<Protos.TaskInfo>) SerializationUtils.deserialize(existingNodes);
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
