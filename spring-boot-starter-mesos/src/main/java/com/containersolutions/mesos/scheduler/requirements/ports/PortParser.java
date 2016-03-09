package com.containersolutions.mesos.scheduler.requirements.ports;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Converts the map of properties into usable ports
 */
public class PortParser {
    private final MesosConfigProperties mesosConfigProperties;

    @Autowired
    public PortParser(MesosConfigProperties mesosConfigProperties) {
        this.mesosConfigProperties = mesosConfigProperties;
    }

    public List<PortFactory.PortFunction> getPorts() {
        return mesosConfigProperties.getResources().getPorts().entrySet().stream()
                .map(PortFactory::create)
                .collect(Collectors.toList());
    }

    public Integer size() {
        return mesosConfigProperties.getResources().getPorts().size();
    }

    @Override
    public String toString() {
        return Arrays.toString(getPorts().toArray());
    }
}
