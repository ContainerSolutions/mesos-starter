package com.containersolutions.mesos.scheduler.requirements;

import org.apache.mesos.Protos;

import java.util.List;

import static java.util.Arrays.asList;

public class OfferEvaluation {
    String requirement;
    Protos.Offer offer;
    List<Protos.Resource> resources;
    boolean valid;
    String taskId;

    public OfferEvaluation(String requirement, String taskId, Protos.Offer offer, boolean valid, List<Protos.Resource> resources) {
        this.requirement = requirement;
        this.taskId = taskId;
        this.offer = offer;
        this.resources = resources;
        this.valid = valid;
    }

    public OfferEvaluation(String requirement, String taskId, Protos.Offer offer, boolean valid, Protos.Resource ... resources) {
        this(requirement, taskId, offer, valid, asList(resources));
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
}
