package dk.mwl.mesos.scheduler.events;

import org.apache.mesos.Protos;
import org.springframework.context.ApplicationEvent;

public class FrameworkReregistreredEvent extends MesosEvent {
    public FrameworkReregistreredEvent(Protos.MasterInfo masterInfo) {
        super(masterInfo);
    }
}
