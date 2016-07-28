package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

import java.sql.Timestamp;

public class TaskDescription {
    private VirtualOffer virtualOffer;
    private final Protos.TaskInfo taskInfo;
    private String taskId;

    public TaskDescription(VirtualOffer virtualOffer, Protos.TaskInfo taskInfo) {
        this.virtualOffer = virtualOffer;
        this.taskInfo = taskInfo;
    }

    public VirtualOffer getVirtualOffer() {
        return virtualOffer;
    }

    public Protos.TaskInfo getTaskInfo() {
        return taskInfo;
    }


    public String getTaskId() {
        return taskId;
    }
}
