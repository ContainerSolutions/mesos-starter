package com.containersolutions.mesos.scheduler.state;

import com.containersolutions.mesos.scheduler.TaskDescription;
import org.apache.mesos.Protos;

import java.util.Optional;
import java.util.Set;

public interface StateRepository {
    Optional<Protos.FrameworkID> getFrameworkID();

    void store(TaskDescription taskDescription);

    Set<TaskDescription> allTaskDescriptions();
}
