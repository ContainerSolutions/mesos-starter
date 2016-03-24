package com.containersolutions.mesos.scheduler.config;

import java.util.LinkedHashMap;
import java.util.Map;

public class ResourcesConfigProperties {

    private double cpus;
    private double mem;

    private Map<String, ResourcePortConfigProperties> ports = new LinkedHashMap<>();

    public double getCpus() {
        return cpus;
    }

    public void setCpus(double cpus) {
        this.cpus = cpus;
    }

    public double getMem() {
        return mem;
    }

    public void setMem(double mem) {
        this.mem = mem;
    }

    public Map<String, ResourcePortConfigProperties> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, ResourcePortConfigProperties> ports) {
        this.ports = ports;
    }
}
