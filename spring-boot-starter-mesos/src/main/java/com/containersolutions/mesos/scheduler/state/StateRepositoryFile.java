package com.containersolutions.mesos.scheduler.state;

import com.containersolutions.mesos.scheduler.TaskDescription;
import com.containersolutions.mesos.scheduler.events.FrameworkRegistreredEvent;
import com.containersolutions.mesos.utils.MesosHelper;
import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.SerializationUtils;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

public class StateRepositoryFile implements StateRepository {
    protected final Log logger = LogFactory.getLog(getClass());

    AtomicReference<Protos.FrameworkID> frameworkId = new AtomicReference<>();

    File stateHome = new File(".state");

    @Value("${mesos.framework.name:default}")
    String frameworkName;

    @Override
    public Optional<Protos.FrameworkID> getFrameworkID() {
        if (!stateHome.exists()) {
            stateHome.mkdir();
            return Optional.empty();
        }
        try {
            File frameworkid = new File(frameworkStateHome(), "frameworkid");
            if (!frameworkid.exists()) {
                return Optional.empty();
            }
            return Optional.of(Protos.FrameworkID.newBuilder().setValue(new String(FileCopyUtils.copyToByteArray(frameworkid))).build());
        } catch (IOException e) {
            throw new RuntimeException("Failed to open frameworkId");
        }
    }

    @EventListener
    public void onFrameworkRegistered(FrameworkRegistreredEvent event) {
        logger.debug("Received frameworkId=" + event.getFrameworkID().getValue());
        frameworkId.set(event.getFrameworkID());
    }

    @EventListener
    public void onStatusUpdate(StatusUpdateEvent event) {
        if (MesosHelper.isTerminalTaskState(event.getTaskStatus().getState())) {
            final Set<TaskDescription> taskDescriptions = allTaskDescriptions();
            final Optional<TaskDescription> taskInfo = taskDescriptions.stream().filter(task -> task.getTaskId().equals(event.getTaskStatus().getTaskId().getValue())).findFirst();
            taskInfo.ifPresent(taskDescriptions::remove);
            save(taskDescriptions);
        }
    }

    @PreDestroy
    public void onExit() {
        frameworkStateHome().delete();
    }

    private void save(Set<TaskDescription> taskIDs) {
        try {
            if (!stateHome.exists()) {
                logger.info("Creating stateHome directory: " + stateHome.getAbsolutePath());
                stateHome.mkdirs();
                FileCopyUtils.copy(frameworkId.get().toByteArray(), new File(frameworkStateHome(), "frameworkid"));
            }
            FileCopyUtils.copy(SerializationUtils.serialize(new HashSet<>(taskIDs)), tasksFile());
        } catch (IOException e) {
            logger.error("Failed to save taskID list", e);
        }
    }

    @Override
    public void store(TaskDescription taskDescription) {
        final Set<TaskDescription> taskDescriptions = allTaskDescriptions();
        taskDescriptions.add(taskDescription);
        save(taskDescriptions);
    }

    @Override
    public Set<TaskDescription> allTaskDescriptions() {
        if (frameworkId.get() == null) {
            logger.warn("Attempted to fetch TaskInfo list before framework has been registered");
            return new HashSet<>();
        }

        final File stateFile = tasksFile();
        try {
            return (Set<TaskDescription>) SerializationUtils.deserialize(FileCopyUtils.copyToByteArray(stateFile));
        } catch (Exception e) {
//            logger.error("Failed to read file: " + stateFile.getAbsolutePath(), e);
            return new HashSet<>();
        }
    }

    private File tasksFile() {
        final File stateFile = new File(frameworkStateHome(), "tasks");
        if (!stateFile.exists()) {
            try {
                stateFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create state file", e);
            }
        }
        return stateFile;
    }

    private File frameworkStateHome() {
        File file = new File(stateHome, frameworkName);
        if (!file.exists()) {
            file.mkdir();
        }
        return file;
    }
}
