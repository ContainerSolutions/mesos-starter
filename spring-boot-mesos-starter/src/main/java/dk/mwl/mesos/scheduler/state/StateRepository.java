package dk.mwl.mesos.scheduler.state;

import org.apache.mesos.Protos;

import java.util.Optional;
import java.util.Set;

public interface StateRepository {
    Optional<Protos.FrameworkID> getFrameworkID();

    void store(Protos.TaskInfo taskInfo);

    Set<Protos.TaskInfo> allTaskInfos();
}
