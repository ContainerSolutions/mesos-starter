package com.containersolutions.mesos.scheduler.config;

import java.util.HashMap;
import java.util.Map;

public class ResourcesConfigProperties {

    private double cpus;

    private Map<String, ResourcePortConfigProperties> ports = new HashMap<>();

    public double getCpus() {
        return cpus;
    }

    public void setCpus(double cpus) {
        this.cpus = cpus;
    }

    public Map<String, ResourcePortConfigProperties> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, ResourcePortConfigProperties> ports) {
        this.ports = ports;
    }
}
