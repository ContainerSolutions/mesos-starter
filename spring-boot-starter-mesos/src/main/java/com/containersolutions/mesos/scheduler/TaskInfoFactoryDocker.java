package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.PortMapping;
import com.containersolutions.mesos.scheduler.requirements.VolumeMapping;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;

public class TaskInfoFactoryDocker implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${mesos.docker.image}")
    protected String dockerImage;

    @Value("${spring.application.name}")
    protected String applicationName;

    @Value("${mesos.docker.network:BRIDGE}")
    protected String networkMode; // May be BRIDGE or HOST

    private final MesosConfigProperties mesosConfig;

    private final MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory;

    public TaskInfoFactoryDocker(MesosConfigProperties mesosConfig, MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory) {
        this.mesosConfig = mesosConfig;
        this.commandInfoMesosProtoFactory = commandInfoMesosProtoFactory;
    }

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
                        .addAllVolumes(volumeMappings(executionParameters.getVolumeMappings()))
                )
                .setCommand(command(executionParameters.getEnvironmentVariables()))
                .build();
    }

    private Iterable<? extends Protos.Volume> volumeMappings(List<VolumeMapping> volumeMappings) {
        return volumeMappings.stream()
                .map(volumeMapping -> Protos.Volume.newBuilder()
                        .setHostPath(volumeMapping.getHostPath())
                        .setContainerPath(volumeMapping.getContainerPath())
                        .setMode(volumeMapping.isReadOnly() ? Protos.Volume.Mode.RO : Protos.Volume.Mode.RW)
                        .build())
                .peek(volume -> logger.info("Mapping host volume to container volume"))
                .collect(Collectors.toList());
    }

    private Protos.CommandInfo command(Map<String, String> additionalEnvironmentVariables) {
        return commandInfoMesosProtoFactory.create(additionalEnvironmentVariables);
    }

    private Iterable<? extends Protos.ContainerInfo.DockerInfo.PortMapping> portMappings(List<PortMapping> portMappings) {
        if (networkMode.equalsIgnoreCase("BRIDGE")) {
            return portMappings.stream()
                    .map(portMapping -> Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder()
                            .setHostPort(portMapping.getOfferedPort())
                            .setContainerPort(portMapping.getContainerPort().orElseThrow(() -> new IllegalArgumentException("No container port specified for " + portMapping.getName())))
                            .build())
                    .peek(portMapping -> logger.info("Mapped host port " + portMapping.getHostPort() + " to container port " + portMapping.getContainerPort()))
                    .collect(Collectors.toList());
        } else {
            return emptyList();
        }
    }
}
