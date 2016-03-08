package com.containersolutions.mesos.scheduler.requirements.ports;

import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.mesos.Protos;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class PrivilegedPort extends StarterPort {
    public PrivilegedPort(String portName) {
        super(ResourcesConfigProperties.PortType.PRIVILEDGED, portName);
    }

    @Override
    public List<Protos.Port> apply(Set<Long> availablePorts) {
        return availablePorts.stream()
                .filter(privPort -> privPort < 1024)
                .limit(1)
                .map(port -> Protos.Port.newBuilder().setName(portName).setNumber(port.intValue()).build())
                .collect(Collectors.toList());
    }
}
