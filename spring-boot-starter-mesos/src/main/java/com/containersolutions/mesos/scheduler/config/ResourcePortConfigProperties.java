package com.containersolutions.mesos.scheduler.config;

public class ResourcePortConfigProperties {
    String host;
    int container = 0;
    String role;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getContainer() {
        return container;
    }

    public void setContainer(int container) {
        this.container = container;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
