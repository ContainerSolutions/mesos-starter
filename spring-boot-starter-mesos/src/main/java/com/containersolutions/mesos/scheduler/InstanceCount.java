package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.events.InstanceCountChangeEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.util.concurrent.atomic.AtomicInteger;

@ManagedResource
public class InstanceCount {
    final AtomicInteger count;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    public InstanceCount(int count) {
        this.count = new AtomicInteger(count);
    }

    @ManagedAttribute
    public int getCount() {
        return count.get();
    }

    @ManagedAttribute
    public void setCount(int count) {
        this.count.set(count);
        applicationEventPublisher.publishEvent(new InstanceCountChangeEvent(count));
    }
}
