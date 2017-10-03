package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.requirements.ResourceRequirement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OfferStrategyFilter {
    protected final Log logger = LogFactory.getLog(getClass());

    private final Map<String, ResourceRequirement> resourceRequirements;

    public OfferStrategyFilter(Map<String, ResourceRequirement> resourceRequirements) {
        this.resourceRequirements = resourceRequirements;
    }

    public OfferEvaluation evaluate(String taskId, Protos.Offer offer) {
        List<OfferEvaluation> offerEvaluations = resourceRequirements.entrySet().stream()
                .map(kv -> kv.getValue().check(kv.getKey(), taskId, offer))
                .peek(offerEvaluation -> logger.debug((offerEvaluation.isValid() ? "Accepting" : "Rejecting") + " offer offerId=" + offer.getId().getValue() + ", by requirement=" + offerEvaluation.getRequirement() + offerEvaluation.getDeclineReason().map(reason -> " with reason=" + reason).orElse("")))
                .collect(Collectors.toList());

        if (offerEvaluations.stream().allMatch(OfferEvaluation::isValid)) {
            return OfferEvaluation.accept(
                    "*",
                    taskId,
                    offer,
                    offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getEnvironmentVariables().entrySet().stream()).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                    offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getPortMappings().stream()).collect(Collectors.toList()),
                    offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getVolumeMappings().stream()).collect(Collectors.toList()),
                    offerEvaluations.stream().flatMap(offerEvaluation -> offerEvaluation.getResources().stream()).collect(Collectors.toList())
            );
        }
        return OfferEvaluation.decline("*", taskId, offer, null);
    }

}
