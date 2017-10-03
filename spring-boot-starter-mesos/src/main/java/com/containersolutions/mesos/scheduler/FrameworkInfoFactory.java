package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.Optional;

/**
 * Creates framework info
 */
public class FrameworkInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${spring.application.name}")
    protected String applicationName;

    private final MesosConfigProperties mesosConfig;
    private final StateRepository stateRepository;
    private final CredentialFactory credentialFactory;

    public FrameworkInfoFactory(MesosConfigProperties mesosConfig, StateRepository stateRepository, CredentialFactory credentialFactory) {
        this.mesosConfig = mesosConfig;
        this.stateRepository = stateRepository;
        this.credentialFactory = credentialFactory;
    }

    public Protos.FrameworkInfo.Builder create() {
        Protos.FrameworkInfo.Builder frameworkBuilder = Protos.FrameworkInfo.newBuilder()
                .setName(applicationName)
                .setUser("root")
                .addRoles(mesosConfig.getRole())
                .addCapabilities(Protos.FrameworkInfo.Capability.newBuilder().setType(Protos.FrameworkInfo.Capability.Type.MULTI_ROLE).build())
                .setCheckpoint(true)
                .setFailoverTimeout(60.0)
                .setId(stateRepository.getFrameworkID().orElseGet(() -> Protos.FrameworkID.newBuilder().setValue("").build()));
        Protos.Credential credential = credentialFactory.create();
        if (credential.isInitialized()) {
            logger.debug("Adding framework principal: " + credential.getPrincipal());
            frameworkBuilder.setPrincipal(credential.getPrincipal());
        }
        Optional.ofNullable(mesosConfig.getWebuiUrl()).ifPresent(frameworkBuilder::setWebuiUrl);
        return frameworkBuilder;
    }
}
