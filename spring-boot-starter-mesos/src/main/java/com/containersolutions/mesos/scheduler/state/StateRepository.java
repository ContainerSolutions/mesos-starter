package com.containersolutions.mesos.scheduler.state;

import org.apache.mesos.Protos;

import java.util.Collection;
import java.util.Optional;
import java.util.Set;

public interface StateRepository {
    Optional<Protos.FrameworkID> getFrameworkID();

    void store(Collection<Protos.TaskInfo> taskInfos);

    Set<Protos.TaskInfo> allTaskInfos();
}
