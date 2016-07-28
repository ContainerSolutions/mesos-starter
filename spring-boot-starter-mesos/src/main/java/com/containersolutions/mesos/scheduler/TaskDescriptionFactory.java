package com.containersolutions.mesos.scheduler;

public interface TaskDescriptionFactory {

    TaskDescription create(VirtualOffer virtualOffer);
}
