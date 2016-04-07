package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.List;

import static java.util.Arrays.asList;

class TaskProposal {
    private final Protos.Offer offer;
    private final List<Protos.TaskInfo> taskInfos;

    public TaskProposal(Protos.Offer offer, Protos.TaskInfo ... taskInfos) {
        this.offer = offer;
        this.taskInfos = asList(taskInfos);
    }

    public Protos.OfferID getOfferId() {
        return offer.getId();
    }

    public List<Protos.TaskInfo> getTaskInfos() {
        return taskInfos;
    }
}
