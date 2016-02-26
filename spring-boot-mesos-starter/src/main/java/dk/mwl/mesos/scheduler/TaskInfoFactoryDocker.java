package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.config.MesosConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

public class TaskInfoFactoryDocker implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${mesos.docker.image}")
    protected String dockerImage;

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired
    MesosConfigProperties mesosConfig;

    @Autowired
    MesosProtoFactory<Protos.CommandInfo.Builder> commandInfoMesosProtoFactory;

    @Override
    public Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources) {
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
                                .addAllPortMappings(portMappings(resources))
                                .setNetwork(Protos.ContainerInfo.DockerInfo.Network.BRIDGE)
                        )
                )
                .setCommand(command())
                .build();
    }

    private Protos.CommandInfo command() {
        return commandInfoMesosProtoFactory.create()
                .setContainer(Protos.CommandInfo.ContainerInfo.newBuilder().setImage(dockerImage))
                .build();
    }

    private Iterable<? extends Protos.ContainerInfo.DockerInfo.PortMapping> portMappings(List<Protos.Resource> resources) {
        Iterator<String> portsIterator = mesosConfig.getResources().getPort().iterator();
        return resources.stream()
                .filter(Protos.Resource::hasRanges)
                .filter(resource -> resource.getName().equals("ports"))
                .flatMap(resource -> resource.getRanges().getRangeList().stream())
                .flatMapToLong(range -> LongStream.rangeClosed(range.getBegin(), range.getEnd()))
                .limit(mesosConfig.getResources().getPort().size())
                .mapToObj(hostPort -> Protos.ContainerInfo.DockerInfo.PortMapping.newBuilder().setHostPort((int) hostPort).setContainerPort(Integer.parseInt(portsIterator.next())).build())
                .peek(portMapping -> logger.debug("Mapped host=" + portMapping.getHostPort() + "=>" + portMapping.getContainerPort()))
                .collect(Collectors.toList());
    }
}
