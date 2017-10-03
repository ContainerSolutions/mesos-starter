package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import org.apache.mesos.Protos;

import java.util.stream.Collectors;

public class TaskMaterializerMinimal implements TaskMaterializer {
    private final TaskInfoFactory taskInfoFactory;

    public TaskMaterializerMinimal(TaskInfoFactory taskInfoFactory) {
        this.taskInfoFactory = taskInfoFactory;
    }

    @Override
    public TaskProposal createProposal(OfferEvaluation offerEvaluation) {
        return new TaskProposal(
                offerEvaluation.getOffer(),
                taskInfoFactory.create(
                        offerEvaluation.getTaskId(),
                        offerEvaluation.getOffer(),
                        offerEvaluation.getResources().stream().filter(resource -> resource.getType() != Protos.Value.Type.RANGES || !resource.getRanges().getRangeList().isEmpty()).collect(Collectors.toList()),
                        new ExecutionParameters(
                                offerEvaluation.getEnvironmentVariables(),
                                offerEvaluation.getPortMappings(),
                                offerEvaluation.getVolumeMappings()
                        )
                )
        );
    }
}
