package com.containersolutions.mesos.scheduler.requirements.ports;

import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.mesos.Protos;

import java.util.List;
import java.util.Set;

public abstract class StarterPort implements PortFactory.PortFunction {
    final ResourcesConfigProperties.PortType portType;
    final String portName;

    public StarterPort(ResourcesConfigProperties.PortType portType, String portName) {
        this.portType = portType;
        this.portName = portName;
    }

    @Override
    public String toString() {
        return "{" + portName + ": " + portType + "}";
    }

    public abstract List<Protos.Port> apply(Set<Integer> availablePorts);
}
