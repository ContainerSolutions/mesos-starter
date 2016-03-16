package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskMaterializerMinimal implements TaskMaterializer {
    @Autowired
    TaskInfoFactory taskInfoFactory;

    @Override
    public TaskProposal createProposal(OfferEvaluation offerEvaluation) {
        return new TaskProposal(
                offerEvaluation.getOffer(),
                taskInfoFactory.create(
                        offerEvaluation.getTaskId(),
                        offerEvaluation.getOffer(),
                        offerEvaluation.getResources(),
                        new ExecutionParameters(offerEvaluation.getEnvironmentVariables(),
                                offerEvaluation.getPortMappings()
                        )
                )
        );
    }
}
