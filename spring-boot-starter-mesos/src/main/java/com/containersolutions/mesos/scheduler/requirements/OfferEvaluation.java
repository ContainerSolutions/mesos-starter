package com.containersolutions.mesos.scheduler.requirements;

import org.apache.mesos.Protos;

import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class OfferEvaluation {
    String requirement;
    Protos.Offer offer;
    boolean valid;
    String taskId;
    private Map<String, String> environmentVariables;
    final List<PortMapping> portMappings;
    List<Protos.Resource> resources;

    public OfferEvaluation(String requirement, String taskId, Protos.Offer offer, boolean valid, Map<String, String> environmentVariables, List<PortMapping> portMappings, List<Protos.Resource> resources) {
        this.requirement = requirement;
        this.taskId = taskId;
        this.offer = offer;
        this.environmentVariables = environmentVariables;
        this.portMappings = portMappings;
        this.resources = resources;
        this.valid = valid;
    }

    public OfferEvaluation(String requirement, String taskId, Protos.Offer offer, boolean valid, Map<String, String> environmentVariables, List<PortMapping> portMappings, Protos.Resource... resources) {
        this(requirement, taskId, offer, valid, environmentVariables, portMappings, asList(resources));
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
}
