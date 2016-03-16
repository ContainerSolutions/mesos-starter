package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.PortMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TaskInfoFactoryDocker implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${mesos.docker.image}")
    protected String dockerImage;

    @Value("${spring.application.name}")
    protected String applicationName;

    @Value("${mesos.docker.network:BRIDGE}")
    protected String networkMode; // May be BRIDGE or HOST

    @Autowired
    MesosConfigProperties mesosConfig;

    @Autowired
    MesosProtoFactory<Protos.CommandInfo.Builder, Map<String, String>> commandInfoMesosProtoFactory;

    @Override
    public Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources, ExecutionParameters executionParameters) {
        logger.info("Creating task with taskId=" + taskId + " from offerId=" + offer.getId().getValue());
        return Protos.TaskInfo.newBuilder()
                .setName(applicationName + ".task")
                .setSlaveId(offer.getSlaveId())
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .addAllResources(resources)
                .setContainer(Protos.ContainerInfo.newBuilder()
                        .setType(Protos.ContainerInfo.Type.DOCKER)
                        .setDocker(Protos.ContainerInfo.DockerInfo.newBuilder()
                                .setImage(dockerImage)
                                .addAllPortMappings(portMappings(executionParameters.getPortMappings()))
                                .setNetwork(Protos.ContainerInfo.DockerInfo.Network.valueOf(networkMode))
                        )
                )
                .setCommand(command(executionParameters.getEnvironmentVariables()))
                .build();
    }

    private Protos.CommandInfo command(Map<String, String> additionalEnvironmentVariables) {
        return commandInfoMesosProtoFactory.create(additionalEnvironmentVariables)
                .setContainer(Protos.CommandInfo.ContainerInfo.newBuilder().setImage(dockerImage))
                .build();
    }

    private Iterable<? extends Protos.ContainerInfo.DockerInfo.PortMapping> portMappings(List<PortMapping> portMappings) {

        return portMappings.stream()
                .map(portMapping -> Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder()
                        .setHostPort(portMapping.getOfferedPort())
                        .setContainerPort(portMapping.getContainerPort().orElseThrow(() -> new IllegalArgumentException("No container port specified for " + portMapping.getName())))
                        .build())
                .peek(portMapping -> logger.info("Mapped host port " + portMapping.getHostPort() + " to container port " + portMapping.getContainerPort()))
                .collect(Collectors.toList());
    }
}
