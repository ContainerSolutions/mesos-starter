package dk.mwl.mesos.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Optional;

public class TaskInfoFactoryDocker implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${mesos.docker.image}")
    protected String dockerImage;

    @Value("${mesos.docker.command}")
    protected Optional<String> command;

    @Value("${spring.application.name}")
    protected String applicationName;

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
                        )
                )
                .setCommand(command()
                )
                .build();
    }

    private Protos.CommandInfo.Builder command() {
        Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
        builder.setContainer(Protos.CommandInfo.ContainerInfo.newBuilder().setImage(dockerImage));
        builder.setShell(command.isPresent());
        command.ifPresent(builder::setValue);
        return builder;
    }


}
