package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;

class TaskProposal {
    Protos.Offer offer;
    Protos.TaskInfo taskInfo;

    public TaskProposal(Protos.Offer offer, Protos.TaskInfo taskInfo) {
        this.offer = offer;
        this.taskInfo = taskInfo;
    }

    public Protos.OfferID getOfferId() {
        return offer.getId();
    }

    public Protos.TaskInfo getTaskInfo() {
        return taskInfo;
    }
}
