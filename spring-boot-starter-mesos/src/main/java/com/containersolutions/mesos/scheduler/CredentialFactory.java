package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.google.protobuf.ByteString;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Creates credentials from user supplied principals and secrets
 */
public class CredentialFactory {
    @Autowired
    MesosConfigProperties mesosConfig;

    public Protos.Credential create() {
        return Protos.Credential.newBuilder()
                .setPrincipal(mesosConfig.getPrincipal())
                .setSecret(ByteString.copyFrom(mesosConfig.getSecret().getBytes()))
                .build();
    }
}
