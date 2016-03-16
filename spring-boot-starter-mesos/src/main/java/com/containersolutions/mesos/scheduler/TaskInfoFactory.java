package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.List;
import java.util.Map;

public interface TaskInfoFactory {

    Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources, ExecutionParameters executionParameters);
}
