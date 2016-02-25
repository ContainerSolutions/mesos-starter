package dk.mwl.mesos.scheduler.events;

import org.springframework.context.ApplicationEvent;

public abstract class MesosEvent extends ApplicationEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public MesosEvent(Object source) {
        super(source);
    }
}
