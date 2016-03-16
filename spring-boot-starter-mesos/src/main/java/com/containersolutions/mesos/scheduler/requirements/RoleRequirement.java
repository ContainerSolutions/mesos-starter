package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class RoleRequirement implements ResourceRequirement {
    @Autowired
    MesosConfigProperties mesosConfig;

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        List<Protos.Resource> roleResources = offer.getResourcesList().stream()
                .filter(Protos.Resource::hasRole)
                .filter(resource -> resource.getRole().equals(mesosConfig.getRole()))
                .collect(Collectors.toList());
        return new OfferEvaluation(
                requirement, taskId, offer,
                !roleResources.isEmpty(),
                Collections.emptyMap(),
                Collections.emptyList(),
                roleResources);
    }
}
