package com.containersolutions.mesos.scheduler.config;

import java.util.Map;

public class ResourcesConfigProperties {

    public enum PortType {ANY, UNPRIVILEDGED, FIXED, PRIVILEDGED}

    private double cpus;

    private Map<String, String> ports;

    public double getCpus() {
        return cpus;
    }

    public void setCpus(double cpus) {
        this.cpus = cpus;
    }

    public Map<String, String> getPorts() {
        return ports;
    }

    public void setPorts(Map<String, String> ports) {
        this.ports = ports;
    }
}
