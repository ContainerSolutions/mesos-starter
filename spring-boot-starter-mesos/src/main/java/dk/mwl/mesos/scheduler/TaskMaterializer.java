package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.requirements.OfferEvaluation;

public interface TaskMaterializer {
    TaskProposal createProposal(OfferEvaluation offerEvaluation);
}
