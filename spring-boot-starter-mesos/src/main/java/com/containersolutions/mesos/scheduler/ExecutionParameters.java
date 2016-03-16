package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.PortMapping;

import java.util.List;
import java.util.Map;

public class ExecutionParameters {
    Map<String, String> environmentVariables;
    List<PortMapping> portMappings;

    public ExecutionParameters(Map<String, String> environmentVariables, List<PortMapping> portMappings) {
        this.environmentVariables = environmentVariables;
        this.portMappings = portMappings;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public List<PortMapping> getPortMappings() {
        return portMappings;
    }
}
