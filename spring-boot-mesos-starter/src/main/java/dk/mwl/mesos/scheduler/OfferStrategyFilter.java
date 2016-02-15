package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public class OfferStrategyFilter {
    @Autowired
    List<ResourceRequirement> resourceRequirements;

    public void accept(List<Protos.Offer> offers) {
        offers.stream()
                .filter(offer -> resourceRequirements.stream().allMatch(requirement -> requirement.test(offer)));
    }
}
