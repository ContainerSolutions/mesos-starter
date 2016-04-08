package com.containersolutions.mesos.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ConfigurationProperties(prefix = "mesos")
public class MesosConfigProperties {
    private String master;
    private ZookeeperConfigProperties zookeeper;
    private String principal;
    private String secret;
    List<TaskConfigProperties> tasks = new ArrayList<>();

    public String getMaster() {
        return master;
    }

    public void setMaster(String master) {
        this.master = master;
    }

    public ZookeeperConfigProperties getZookeeper() {
        return zookeeper;
    }

    public void setZookeeper(ZookeeperConfigProperties zookeeper) {
        this.zookeeper = zookeeper;
    }

    public String getPrincipal() {
        return principal;
    }

    public void setPrincipal(String principal) {
        this.principal = principal;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public List<TaskConfigProperties> getTasks() {
        return tasks;
    }

    public void setTasks(List<TaskConfigProperties> tasks) {
        this.tasks = tasks;
    }
}
