package com.containersolutions.mesos.scheduler.events;

import org.apache.mesos.Protos;

public class FrameworkReregistreredEvent extends MesosEvent {
    public FrameworkReregistreredEvent(Protos.MasterInfo masterInfo) {
        super(masterInfo);
    }
}
