package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public interface ResourceRequirement extends Function<Protos.Offer, OfferEvaluation> {

    static double scalarSum(Protos.Offer offer, String name) {
        return offer.getResourcesList().stream().filter(resource -> resource.getName().equals(name)).mapToDouble(resource -> resource.getScalar().getValue()).sum();
    }
}
