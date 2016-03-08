package com.containersolutions.mesos.scheduler.requirements.ports;

import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.mesos.Protos;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class UnprivilegedPort extends StarterPort {
    public UnprivilegedPort(String portName) {
        super(ResourcesConfigProperties.PortType.UNPRIVILEDGED, portName);
    }

    @Override
    public List<Protos.Port> apply(Set<Long> availablePorts) {
        return availablePorts.stream()
                .filter(unprivPort -> unprivPort >= 1024)
                .limit(1)
                .map(port -> Protos.Port.newBuilder().setName(portName).setNumber(port.intValue()).build())
                .collect(Collectors.toList());
    }
}
