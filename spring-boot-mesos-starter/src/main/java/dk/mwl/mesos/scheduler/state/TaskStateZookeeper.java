package dk.mwl.mesos.scheduler.state;

import org.apache.mesos.Protos;
import org.apache.mesos.state.State;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.Set;

public class TaskStateZookeeper implements StateRepository {
    @Autowired
    State zkState;

    @Override
    public Optional<Protos.FrameworkID> getFrameworkID() {
        return null;
    }

    @Override
    public void store(Protos.TaskInfo taskInfo) {

    }

    @Override
    public Set<Protos.TaskInfo> allTaskInfos() {
        return null;
    }
}
