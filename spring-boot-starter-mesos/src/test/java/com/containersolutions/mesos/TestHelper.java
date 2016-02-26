package com.containersolutions.mesos;

import org.apache.mesos.Protos;

import java.util.function.Consumer;

public class TestHelper {
    public static Protos.Offer createDummyOffer() {
        return createDummyOffer(null);
    }

    public static Protos.Offer createDummyOffer(Consumer<Protos.Offer.Builder> mapper) {
        final Protos.Offer.Builder offer = Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue("offer id"))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("framework id"))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave id"))
                .setHostname("hostname");
        if (mapper != null) {
            mapper.accept(offer);
        }
        return offer.build();

    }

    public static Protos.TaskInfo createDummyTask() {
        return createDummyTask(null);
    }

    public static Protos.TaskInfo createDummyTask(Consumer<Protos.TaskInfo.Builder> mapper) {
        Protos.TaskInfo.Builder task = Protos.TaskInfo.newBuilder()
                .setName("task")
                .setTaskId(Protos.TaskID.newBuilder().setValue("task id"))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave id"));
        if (mapper != null) {
            mapper.accept(task);
        }
        return task.build();
    }
}
