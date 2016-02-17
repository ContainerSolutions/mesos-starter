package dk.mwl.mesos.scheduler.events;

import org.apache.mesos.Protos;
import org.springframework.context.ApplicationEvent;

public class StatusUpdateEvent extends MesosEvent {
    public StatusUpdateEvent(Protos.TaskStatus taskStatus) {
        super(taskStatus);
    }

    public Protos.TaskStatus getTaskStatus() {
        return (Protos.TaskStatus) getSource();
    }
}
