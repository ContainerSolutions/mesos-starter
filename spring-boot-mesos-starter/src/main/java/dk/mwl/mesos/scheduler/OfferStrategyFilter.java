package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.stream.Collectors;

public class OfferStrategyFilter {
    @Autowired
    List<ResourceRequirement> resourceRequirements;

    public OfferEvaluation evaluate(String taskId, Protos.Offer offer) {
        List<OfferEvaluation> offerEvaluations = resourceRequirements.stream()
                .map(requirement -> requirement.check(taskId, offer))
                .collect(Collectors.toList());

        return new OfferEvaluation(
                taskId,
                offer,
                offerEvaluations.stream().allMatch(OfferEvaluation::isValid),
                offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.resources.stream()).collect(Collectors.toList())
        );
    }

}
