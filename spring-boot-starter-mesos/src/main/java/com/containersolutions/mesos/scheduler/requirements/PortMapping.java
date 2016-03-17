package com.containersolutions.mesos.scheduler.requirements;

import org.apache.mesos.Protos;

import java.util.OptionalInt;

public class PortMapping {
    String name;
    int offeredPort;
    int containerPort = 0;

    public PortMapping(String name, int offeredPort, int containerPort) {
        this.name = name;
        this.offeredPort = offeredPort;
        this.containerPort = containerPort;
    }

    public String envName() {
        return name.toUpperCase().replace("-", "_");
    }

    public String envValue() {
        return String.valueOf(offeredPort);
    }

    public int getOfferedPort() {
        return offeredPort;
    }

    public OptionalInt getContainerPort() {
        return OptionalInt.of(containerPort);
    }

    Protos.Value.Range toRange() {
        return Protos.Value.Range.newBuilder().setBegin(offeredPort).setEnd(offeredPort).build();
    }

    public String getName() {
        return name;
    }
}
