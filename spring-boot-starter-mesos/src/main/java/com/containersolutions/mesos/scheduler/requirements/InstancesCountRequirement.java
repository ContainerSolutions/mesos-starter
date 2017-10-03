package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.InstanceCount;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;

public class InstancesCountRequirement implements ResourceRequirement {
    private final Log logger = LogFactory.getLog(getClass());

    private final StateRepository stateRepository;

    private final InstanceCount instanceCount;

    public InstancesCountRequirement(StateRepository stateRepository, InstanceCount instanceCount) {
        this.stateRepository = stateRepository;
        this.instanceCount = instanceCount;
    }

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        int totalCount = tasksCount();
        int instanceCount = this.instanceCount.getCount();

        if (totalCount < instanceCount) {
            return OfferEvaluation.accept(requirement, taskId, offer, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        }
        return OfferEvaluation.decline(requirement, taskId, offer, "tasksCount=" + totalCount + " < instanceCount=" + instanceCount);
    }

    private int tasksCount() {
        return stateRepository.allTaskInfos().size();
    }
}
