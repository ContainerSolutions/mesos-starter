package com.containersolutions.mesos.scheduler.events;

/**
 * Remote party has removed the framework.
 */
public class FrameworkRemovedEvent extends MesosEvent {
    public FrameworkRemovedEvent(String message) {
        super(message);
    }
}
