package com.containersolutions.mesos.scheduler.requirements.ports;


import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;

import java.util.Arrays;
import java.util.Map;

public class PortFactory {
    public static PortFunction create(Map.Entry<String, String> portProperty) {
        ResourcesConfigProperties.PortType portType;
        try {
            Long.parseLong(portProperty.getValue());
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
                return new FixedPort(portProperty.getKey(), Long.parseLong(portProperty.getValue()));
            default:
                throw new IllegalArgumentException("Port not numeric or valid port type: " + Arrays.toString(ResourcesConfigProperties.PortType.values()));
        }
    }
}
