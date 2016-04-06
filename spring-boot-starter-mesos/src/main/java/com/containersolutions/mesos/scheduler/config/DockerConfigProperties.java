package com.containersolutions.mesos.scheduler.config;

public class DockerConfigProperties {
    private String image;
    private String network;

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getNetwork() {
        return network;
    }

    public void setNetwork(String network) {
        this.network = network;
    }
}
