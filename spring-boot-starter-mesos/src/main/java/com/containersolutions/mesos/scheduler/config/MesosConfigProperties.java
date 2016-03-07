package com.containersolutions.mesos.scheduler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

@ConfigurationProperties(prefix = "mesos")
public class MesosConfigProperties {
    private String role = "*";
    private String command;
    private Map<String, String> environment = new HashMap<>();
    private ResourcesConfigProperties resources = new ResourcesConfigProperties();
    private String principal;
    private String secret;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Map<String, String> getEnvironment() {
        return environment;
    }

    public void setEnvironment(Map<String, String> environment) {
        this.environment = environment;
    }

    public ResourcesConfigProperties getResources() {
        return resources;
    }

    public void setResources(ResourcesConfigProperties resources) {
        this.resources = resources;
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
}
