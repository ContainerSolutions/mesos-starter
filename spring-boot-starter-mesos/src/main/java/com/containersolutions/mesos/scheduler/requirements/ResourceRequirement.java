package com.containersolutions.mesos.scheduler.requirements;

import org.apache.mesos.Protos;

@FunctionalInterface
@Deprecated
public interface ResourceRequirement {
    OfferEvaluation check(String requirement, String taskId, Protos.Offer offer);

    static double scalarSum(Protos.Offer offer, String name) {
        return offer.getResourcesList().stream().filter(resource -> resource.getName().equals(name)).mapToDouble(resource -> resource.getScalar().getValue()).sum();
    }
}
