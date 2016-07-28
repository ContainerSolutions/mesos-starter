package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

@FunctionalInterface
public interface OfferRequirement {
    boolean check(Protos.Offer offer);
}
