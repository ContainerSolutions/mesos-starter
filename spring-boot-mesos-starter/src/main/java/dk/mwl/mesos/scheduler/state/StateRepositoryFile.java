package dk.mwl.mesos.scheduler.state;

import dk.mwl.mesos.scheduler.events.FrameworkRegistreredEvent;
import dk.mwl.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.context.event.EventListener;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.SerializationUtils;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.Arrays.asList;

public class StateRepositoryFile implements StateRepository {
    protected final Log logger = LogFactory.getLog(getClass());

    AtomicReference<Protos.FrameworkID> frameworkId = new AtomicReference<>();

    File stateHome = new File(".state");

    @Override
    public Optional<Protos.FrameworkID> getFrameworkID() {
        return Arrays.stream(stateHome.listFiles())
                .sorted((f1, f2) -> Long.compare(f1.lastModified(), f2.lastModified()))
                .findFirst()
                .map(file -> Protos.FrameworkID.newBuilder().setValue(file.getName()).build());
    }

    @EventListener
    public void onFrameworkRegistered(FrameworkRegistreredEvent event) {
        logger.debug("Received frameworkId=" + event.getFrameworkID().getValue());
        frameworkId.set(event.getFrameworkID());
    }

    @EventListener
    public void onStatusUpdate(StatusUpdateEvent event) {
        if (asList(Protos.TaskState.TASK_FINISHED, Protos.TaskState.TASK_FAILED, Protos.TaskState.TASK_KILLED, Protos.TaskState.TASK_LOST, Protos.TaskState.TASK_ERROR).contains(event.getTaskStatus().getState())) {
            final Set<Protos.TaskInfo> taskInfos = allTaskInfos();
            final Optional<Protos.TaskInfo> taskInfo = taskInfos.stream().filter(task -> task.getTaskId().equals(event.getTaskStatus().getTaskId())).findFirst();
            taskInfo.ifPresent(taskInfos::remove);
            save(taskInfos);
        }
    }

    @PreDestroy
    public void onExit() {
        file().delete();
    }

    private void save(Set<Protos.TaskInfo> taskIDs) {
        if (!stateHome.exists()) {
            logger.info("Creating stateHome directory: " + stateHome.getAbsolutePath());
            stateHome.mkdirs();
        }
        try {
            FileCopyUtils.copy(SerializationUtils.serialize(new HashSet<>(taskIDs)), file());
        } catch (IOException e) {
            logger.error("Failed to save taskID list", e);
        }
    }

    @Override
    public void store(Protos.TaskInfo taskInfo) {
        final Set<Protos.TaskInfo> taskInfos = allTaskInfos();
        taskInfos.add(taskInfo);
        save(taskInfos);
    }

    @Override
    public Set<Protos.TaskInfo> allTaskInfos() {
        if (frameworkId.get() == null) {
            logger.warn("Attempted to fetch TaskInfo list before framework has been registered");
            return new HashSet<>();
        }

        final File stateFile = file();
        try {
            return (Set<Protos.TaskInfo>) SerializationUtils.deserialize(FileCopyUtils.copyToByteArray(stateFile));
        } catch (Exception e) {
//            logger.error("Failed to read file: " + stateFile.getAbsolutePath(), e);
            return new HashSet<>();
        }
    }

    private File file() {
        final File stateFile = new File(stateHome, frameworkId.get().getValue());
        if (!stateFile.exists()) {
            try {
                stateFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException("Failed to create state file", e);
            }
        }
        return stateFile;
    }
}
