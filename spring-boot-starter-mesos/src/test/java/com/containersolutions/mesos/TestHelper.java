package com.containersolutions.mesos;

import org.apache.mesos.Protos;

import java.util.function.Consumer;

import static com.containersolutions.mesos.config.autoconfigure.MesosSchedulerConfiguration.MESOS_PORTS;

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

    public static Protos.Resource.Builder defaultResource() {
        return Protos.Resource.newBuilder()
                .setName(MESOS_PORTS)
                .setType(Protos.Value.Type.RANGES);
    }

    public static Protos.Offer.Builder defaultOffer() {
        return Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue(""))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue(""))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(""))
                .setHostname("");
    }

    public static Protos.Resource.Builder defaultResource(Integer port) {
        return Protos.Resource.newBuilder()
                .setName("ports")
                .setType(Protos.Value.Type.RANGES)
                .setRanges(Protos.Value.Ranges.newBuilder()
                                .addRange(Protos.Value.Range.newBuilder()
                                        .setBegin(port)
                                        .setEnd(port))
                );
    }
}
