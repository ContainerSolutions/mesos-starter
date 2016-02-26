package com.containersolutions.mesos.scheduler.events;

import org.apache.mesos.Protos;

public class StatusUpdateEvent extends MesosEvent {
    public StatusUpdateEvent(Protos.TaskStatus taskStatus) {
        super(taskStatus);
    }

    public Protos.TaskStatus getTaskStatus() {
        return (Protos.TaskStatus) getSource();
    }
}
