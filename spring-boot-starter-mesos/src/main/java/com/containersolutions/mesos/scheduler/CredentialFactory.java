package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.apache.mesos.Protos;

/**
 * Creates credentials from user supplied principals and secrets
 */
public class CredentialFactory {
    private final MesosConfigProperties mesosConfig;

    public CredentialFactory(MesosConfigProperties mesosConfig) {
        this.mesosConfig = mesosConfig;
    }

    public Protos.Credential create() {
        if (mesosConfig.getPrincipal() != null && mesosConfig.getSecret() != null) {
            return Protos.Credential.newBuilder()
                    .setPrincipal(mesosConfig.getPrincipal())
                    .setSecret(mesosConfig.getSecret())
                    .build();
        } else {
            return Protos.Credential.getDefaultInstance();
        }
    }
}
