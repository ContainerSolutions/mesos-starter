package com.containersolutions.mesos.scheduler.requirements.ports;

import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;

import java.util.Collections;
import java.util.List;
import java.util.Set;

public class FixedPort extends StarterPort {
    protected final Log logger = LogFactory.getLog(getClass());
    private final Long port;

    public FixedPort(String portName, Long port) {
        super(ResourcesConfigProperties.PortType.FIXED, portName);
        this.port = port;
    }

    @Override
    public List<Protos.Port> apply(Set<Long> availablePorts) {
        if (availablePorts.contains(port)) {
            return Collections.singletonList(Protos.Port.newBuilder().setName(portName).setNumber(port.intValue()).build());
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String toString() {
        return "{ " + portName + ": " + port + "}";
    }
}
