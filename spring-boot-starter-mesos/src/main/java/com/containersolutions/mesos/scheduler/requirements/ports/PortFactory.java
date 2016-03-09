package com.containersolutions.mesos.scheduler.requirements.ports;


import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.mesos.Protos;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

public class PortFactory {
    public static PortFunction create(Map.Entry<String, String> portProperty) {
        ResourcesConfigProperties.PortType portType;
        try {
            Integer.parseInt(portProperty.getValue());
            portType = ResourcesConfigProperties.PortType.FIXED;
        } catch (NumberFormatException ignored) {
            try {
                portType = ResourcesConfigProperties.PortType.valueOf(portProperty.getValue());
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Port not numeric or valid port type: " + Arrays.toString(ResourcesConfigProperties.PortType.values()), e);
            }
        }
        switch (portType) {
            case ANY:
            case UNPRIVILEDGED:
                return new UnprivilegedPort(portProperty.getKey());
            case PRIVILEDGED:
                return new PrivilegedPort(portProperty.getKey());
            case FIXED:
                return new FixedPort(portProperty.getKey(), Integer.parseInt(portProperty.getValue()));
            default:
                throw new IllegalArgumentException("Port not numeric or valid port type: " + Arrays.toString(ResourcesConfigProperties.PortType.values()));
        }
    }

    public interface PortFunction extends Function<Set<Integer>, List<Protos.Port>> {
    }
}
