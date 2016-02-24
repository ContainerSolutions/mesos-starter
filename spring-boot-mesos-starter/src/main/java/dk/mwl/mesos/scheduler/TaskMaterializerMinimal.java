package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.requirements.OfferEvaluation;
import org.springframework.beans.factory.annotation.Autowired;

public class TaskMaterializerMinimal implements TaskMaterializer {
    @Autowired
    TaskInfoFactory taskInfoFactory;

    @Override
    public TaskProposal createProposal(OfferEvaluation offerEvaluation) {
        return new TaskProposal(offerEvaluation.getOffer(), taskInfoFactory.create(offerEvaluation.getTaskId(), offerEvaluation.getOffer(), offerEvaluation.getResources()));
    }
}
