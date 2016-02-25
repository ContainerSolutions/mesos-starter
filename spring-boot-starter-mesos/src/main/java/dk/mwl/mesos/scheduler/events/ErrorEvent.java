package dk.mwl.mesos.scheduler.events;

public class ErrorEvent extends MesosEvent {
    public ErrorEvent(String message) {
        super(message);
    }
}
