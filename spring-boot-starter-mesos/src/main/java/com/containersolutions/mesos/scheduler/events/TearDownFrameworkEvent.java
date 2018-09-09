package com.containersolutions.mesos.scheduler.events;

public class TearDownFrameworkEvent extends MesosEvent {
    /**
     * Create a new ApplicationEvent.
     *
     * @param source the component that published the event (never {@code null})
     */
    public TearDownFrameworkEvent(Object source) {
        super(source);
    }
}
