package com.containersolutions.mesos.scheduler.config;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ResourcesConfigProperties {

    private double cpus;
    private double mem;

    private Map<String, ResourcePortConfigProperties> ports = new LinkedHashMap<>();
    private List<ResourceVolumeConfigProperties> volumes = new ArrayList<>();
    private int count;

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

    public List<ResourceVolumeConfigProperties> getVolumes() {
        return volumes;
    }

    public void setVolumes(List<ResourceVolumeConfigProperties> volumes) {
        this.volumes = volumes;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
