package dk.mwl.mesos.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class OfferStrategyFilter {
    @Autowired
    List<ResourceRequirement> resourceRequirements;

    public OfferEvaluation evaluate(Protos.Offer offer) {
        List<OfferEvaluation> offerEvaluations = resourceRequirements.stream()
                .map(requirement -> requirement.apply(offer))
                .collect(Collectors.toList());

        return new OfferEvaluation(
                offer,
                offerEvaluations.stream().allMatch(OfferEvaluation::isValid),
                offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.resources.stream()).collect(Collectors.toList())
        );
    }

}
