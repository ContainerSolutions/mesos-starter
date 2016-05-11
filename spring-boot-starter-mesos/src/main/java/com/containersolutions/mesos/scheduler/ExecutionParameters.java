package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.requirements.PortMapping;
import com.containersolutions.mesos.scheduler.requirements.VolumeMapping;

import java.util.List;
import java.util.Map;

public class ExecutionParameters {
    Map<String, String> environmentVariables;
    List<PortMapping> portMappings;
    private List<VolumeMapping> volumeMappings;

    public ExecutionParameters(Map<String, String> environmentVariables, List<PortMapping> portMappings, List<VolumeMapping> volumeMappings) {
        this.environmentVariables = environmentVariables;
        this.portMappings = portMappings;
        this.volumeMappings = volumeMappings;
    }

    public Map<String, String> getEnvironmentVariables() {
        return environmentVariables;
    }

    public List<PortMapping> getPortMappings() {
        return portMappings;
    }

    public List<VolumeMapping> getVolumeMappings() {
        return volumeMappings;
    }

    public void setVolumes(List<VolumeMapping> volumes) {
        this.volumeMappings = volumes;
    }
}
