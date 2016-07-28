package com.containersolutions.mesos;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.mesos.Protos;

import java.util.function.Consumer;

public class TestHelper {
    public static Protos.Offer createDummyOffer() {
        return createDummyOffer(null);
    }

    public static Protos.Offer createDummyOffer(Consumer<Protos.Offer.Builder> mapper) {
        final Protos.Offer.Builder offer = Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue("offer id " + RandomStringUtils.randomAlphanumeric(5)))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue("framework id"))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave id " + RandomStringUtils.randomAlphanumeric(5)))
                .setHostname("hostname");
        if (mapper != null) {
            mapper.accept(offer);
        }
        return offer.build();

    }

    public static Protos.TaskInfo createDummyTask(String name) {
        return createDummyTask(name, null);
    }

    public static Protos.TaskInfo createDummyTask(String name, Consumer<Protos.TaskInfo.Builder> mapper) {
        Protos.TaskInfo.Builder task = Protos.TaskInfo.newBuilder()
                .setName(name)
                .setTaskId(Protos.TaskID.newBuilder().setValue(name + " id"))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue("slave id"));
        if (mapper != null) {
            mapper.accept(task);
        }
        return task.build();
    }

    public static Protos.Offer createDummyOffer(double cpus, double mem) {
        return createDummyOffer(builder -> {
            builder.addResources(Protos.Resource.newBuilder(Protos.Resource.newBuilder().setName("cpus").setScalar(Protos.Value.Scalar.newBuilder().setValue(cpus)).setType(Protos.Value.Type.SCALAR).build()));
            builder.addResources(Protos.Resource.newBuilder(Protos.Resource.newBuilder().setName("mem").setScalar(Protos.Value.Scalar.newBuilder().setValue(mem)).setType(Protos.Value.Type.SCALAR).build()));
        });
    }
}
