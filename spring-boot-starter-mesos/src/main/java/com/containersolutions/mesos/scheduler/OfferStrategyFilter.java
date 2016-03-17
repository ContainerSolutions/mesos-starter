package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.requirements.ResourceRequirement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OfferStrategyFilter {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    Map<String, ResourceRequirement> resourceRequirements;

    public OfferEvaluation evaluate(String taskId, Protos.Offer offer) {
        List<OfferEvaluation> offerEvaluations = resourceRequirements.entrySet().stream()
                .map(kv -> kv.getValue().check(kv.getKey(), taskId, offer))
                .peek(offerEvaluation -> logger.debug((offerEvaluation.isValid() ? "Accepting" : "Rejecting") + " offer offerId=" + offer.getId().getValue() + ", by requirement=" + offerEvaluation.getRequirement()))
                .collect(Collectors.toList());

        return new OfferEvaluation(
                "*",
                taskId,
                offer,
                offerEvaluations.stream().allMatch(OfferEvaluation::isValid),
                offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getEnvironmentVariables().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getPortMappings().stream()).collect(Collectors.toList()),
                offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getResources().stream()).collect(Collectors.toList())
        );
    }

}
