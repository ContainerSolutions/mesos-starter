package com.containersolutions.mesos.scheduler.requirements;

import org.apache.mesos.Protos;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;

public class OfferEvaluation {
    String requirement;
    Protos.Offer offer;
    boolean valid;
    String declineReason;
    String taskId;
    private Map<String, String> environmentVariables;
    final List<PortMapping> portMappings;
    List<Protos.Resource> resources;
    private List<VolumeMapping> volumeMappings;

    private OfferEvaluation(String requirement, String taskId, Protos.Offer offer, boolean valid, String declineReason, Map<String, String> environmentVariables, List<PortMapping> portMappings, List<VolumeMapping> volumeMappings, List<Protos.Resource> resources) {
        this.requirement = requirement;
        this.taskId = taskId;
        this.offer = offer;
        this.valid = valid;
        this.declineReason = declineReason;
        this.environmentVariables = environmentVariables;
        this.portMappings = portMappings;
        this.volumeMappings = volumeMappings;
        this.resources = resources;
    }

    public static OfferEvaluation accept(String requirement, String taskId, Protos.Offer offer, Map<String, String> environmentVariables, List<PortMapping> portMappings, List<VolumeMapping> volumeMappings, List<Protos.Resource> resources) {
        return new OfferEvaluation(requirement, taskId, offer, true, null, environmentVariables, portMappings, volumeMappings, resources);
    }

    public static OfferEvaluation accept(String requirement, String taskId, Protos.Offer offer, Map<String, String> environmentVariables, List<PortMapping> portMappings, List<VolumeMapping> volumeMappings, Protos.Resource ... resources) {
        return new OfferEvaluation(requirement, taskId, offer, true, null, environmentVariables, portMappings, volumeMappings, asList(resources));
    }

    public static OfferEvaluation decline(String requirement, String taskId, Protos.Offer offer, String reason) {
        return new OfferEvaluation(requirement, taskId, offer, false, reason, null, null, null, null);
    }

    public boolean isValid() {
        return valid;
    }

    public Protos.Offer getOffer() {
        return offer;
    }

    public List<Protos.Resource> getResources() {
        return resources;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getRequirement() {
        return requirement;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public void setEnvironmentVariables(Map<String, String> environmentVariables) {
        this.environmentVariables = environmentVariables;
    }

    public List<PortMapping> getPortMappings() {
        return portMappings;
    }

    public Optional<String> getDeclineReason() {
        return Optional.ofNullable(this.declineReason);
    }

    public List<VolumeMapping> getVolumeMappings() {
        return volumeMappings;
    }
}
