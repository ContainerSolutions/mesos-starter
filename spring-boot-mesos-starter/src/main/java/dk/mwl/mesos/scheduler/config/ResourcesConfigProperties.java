package dk.mwl.mesos.scheduler.config;

import java.util.ArrayList;
import java.util.List;

public class ResourcesConfigProperties {

    private double cpus;

    private List<String> port = new ArrayList<>();

    public double getCpus() {
        return cpus;
    }

    public void setCpus(double cpus) {
        this.cpus = cpus;
    }

    public List<String> getPort() {
        return port;
    }

    public void setPort(List<String> port) {
        this.port = port;
    }
}
