package dk.mwl.mesos.scheduler.events;

import org.apache.mesos.Protos;
import org.springframework.context.ApplicationEvent;

public class FrameworkRegistreredEvent extends MesosEvent {
    private final Protos.MasterInfo masterInfo;

    public FrameworkRegistreredEvent(Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        super(frameworkID);
        this.masterInfo = masterInfo;
    }

    public Protos.MasterInfo getMasterInfo() {
        return masterInfo;
    }
}
