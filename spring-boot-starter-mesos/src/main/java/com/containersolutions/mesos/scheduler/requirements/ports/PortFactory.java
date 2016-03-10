package com.containersolutions.mesos.scheduler.requirements.ports;


import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    public static class UnprivilegedPort extends StarterPort {
        public UnprivilegedPort(String portName) {
            super(ResourcesConfigProperties.PortType.UNPRIVILEDGED, portName);
        }

        @Override
        public List<Protos.Port> apply(Set<Integer> availablePorts) {
            return availablePorts.stream()
                    .filter(unprivPort -> unprivPort >= 1024)
                    .limit(1)
                    .map(port -> Protos.Port.newBuilder().setName(portName).setNumber(port).build())
                    .collect(Collectors.toList());
        }
    }

    public static class PrivilegedPort extends StarterPort {
        public PrivilegedPort(String portName) {
            super(ResourcesConfigProperties.PortType.PRIVILEDGED, portName);
        }

        @Override
        public List<Protos.Port> apply(Set<Integer> availablePorts) {
            return availablePorts.stream()
                    .filter(privPort -> privPort < 1024)
                    .limit(1)
                    .map(port -> Protos.Port.newBuilder().setName(portName).setNumber(port).build())
                    .collect(Collectors.toList());
        }
    }

    public static class FixedPort extends StarterPort {
        protected final Log logger = LogFactory.getLog(getClass());
        private final Integer port;

        public FixedPort(String portName, Integer port) {
            super(ResourcesConfigProperties.PortType.FIXED, portName);
            this.port = port;
        }

        @Override
        public List<Protos.Port> apply(Set<Integer> availablePorts) {
            if (availablePorts.contains(port)) {
                return Collections.singletonList(Protos.Port.newBuilder().setName(portName).setNumber(port).build());
            } else {
                return Collections.emptyList();
            }
        }

        @Override
        public String toString() {
            return "{ " + portName + ": " + port + "}";
        }
    }
}
