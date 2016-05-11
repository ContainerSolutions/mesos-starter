package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.stream.Collectors;

public class VolumesRequirement implements ResourceRequirement {
    @Autowired
    MesosConfigProperties mesosConfig;

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        return OfferEvaluation.accept(
                requirement,
                taskId,
                offer,
                Collections.emptyMap(),
                Collections.emptyList(),
                mesosConfig.getResources().getVolumes().stream()
                        .map(properties -> new VolumeMapping(
                                properties.getHostPath(),
                                properties.getContainerPath(),
                                properties.isReadonly()))
                        .collect(Collectors.toList())
                //TODO: Resources needed?
        );
    }
}
