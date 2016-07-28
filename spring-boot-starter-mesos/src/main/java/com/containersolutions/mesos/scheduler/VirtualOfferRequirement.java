package com.containersolutions.mesos.scheduler;

@FunctionalInterface
public interface VirtualOfferRequirement {

    boolean check(VirtualOffer virtualOffer);
}
