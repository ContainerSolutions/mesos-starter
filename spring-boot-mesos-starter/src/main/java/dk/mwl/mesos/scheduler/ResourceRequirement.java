package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;

@FunctionalInterface
public interface ResourceRequirement {
    OfferEvaluation check(String taskId, Protos.Offer offer);

    static double scalarSum(Protos.Offer offer, String name) {
        return offer.getResourcesList().stream().filter(resource -> resource.getName().equals(name)).mapToDouble(resource -> resource.getScalar().getValue()).sum();
    }
}
