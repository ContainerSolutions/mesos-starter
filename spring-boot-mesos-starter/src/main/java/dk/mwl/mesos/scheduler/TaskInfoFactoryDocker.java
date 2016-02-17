package dk.mwl.mesos.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.UUID;

public class TaskInfoFactoryDocker implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${mesos.docker.image}")
    protected String dockerImage;

    @Value("${spring.application.name}")
    protected String applicationName;

    @Override
    public Protos.TaskInfo create(Protos.Offer offer, List<Protos.Resource> resources) {
        final String taskId = UUID.randomUUID().toString();
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
                        )
                )
                .setCommand(Protos.CommandInfo.newBuilder()
                        .setContainer(Protos.CommandInfo.ContainerInfo.newBuilder()
                                .setImage(dockerImage)
                        )
                        .setShell(false)
                )
                .build();
    }
}
