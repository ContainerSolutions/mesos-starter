package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;

import static com.containersolutions.mesos.config.autoconfigure.MesosSchedulerConfiguration.MESOS_PORTS;

public class PortUtil {
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
