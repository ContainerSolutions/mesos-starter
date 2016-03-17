package com.containersolutions.mesos.scheduler.events;

import org.springframework.context.ApplicationEvent;

public class InstanceCountChangeEvent extends ApplicationEvent {
    public InstanceCountChangeEvent(int newCount) {
        super(newCount);
    }

    public int getCount() {
        return (int) getSource();
    }
}
