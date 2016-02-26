package com.containersolutions.mesos.scheduler.events;

public class ErrorEvent extends MesosEvent {
    public ErrorEvent(String message) {
        super(message);
    }
}
