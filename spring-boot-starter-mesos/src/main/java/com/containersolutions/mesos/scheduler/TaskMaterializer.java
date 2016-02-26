package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;

public interface TaskMaterializer {
    TaskProposal createProposal(OfferEvaluation offerEvaluation);
}
