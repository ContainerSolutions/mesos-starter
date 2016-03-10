package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.DockerConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class TaskInfoFactoryDocker implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired
    DockerConfigProperties dockerConfig;

    @Autowired
    MesosProtoFactory<Protos.CommandInfo.Builder> commandInfoMesosProtoFactory;

    @Override
    public Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources) {
        logger.info("Creating task with taskId=" + taskId + " from offerId=" + offer.getId().getValue());

        // Remove ports_env from actual mesos resources. They don't exist.
        List<Protos.Resource> mesosResources = resources.stream().filter(resource -> !resource.getName().equals("ports_env")).collect(Collectors.toList());
        return Protos.TaskInfo.newBuilder()
                .setName(applicationName + ".task")
                .setSlaveId(offer.getSlaveId())
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .addAllResources(mesosResources)
                .setContainer(Protos.ContainerInfo.newBuilder()
                                .setType(Protos.ContainerInfo.Type.DOCKER)
                                .setDocker(Protos.ContainerInfo.DockerInfo.newBuilder()
                                                .addAllParameters(addParameters(commandInfoMesosProtoFactory.create(resources).getEnvironment()))
                                                .setImage(dockerConfig.getImage())
                                                .setNetwork(Protos.ContainerInfo.DockerInfo.Network.valueOf(dockerConfig.getNetwork()))
                                )
                )
                .setCommand(commandInfoMesosProtoFactory.create(resources))
                .build();
    }

    private Iterable<? extends Protos.Parameter> addParameters(Protos.Environment environment) {
        // Do any of the parameters contain env vars? We need to inject them, because the docker command runs before we're in the sandbox
        Map<String, String> dockerParameters = injectEnvVarsIntoParameters(environment);

        return dockerParameters.entrySet().stream()
                .map(entry -> Protos.Parameter.newBuilder().setKey(entry.getKey()).setValue(entry.getValue()).build())
                .collect(Collectors.toList());

    }

    private Map<String, String> injectEnvVarsIntoParameters(Protos.Environment environment) {
        Map<String, String> dockerParameters = new HashMap<>();
        for (Map.Entry<String, String> entry : dockerConfig.getParameter().entrySet()) {
            if (entry.getValue().contains("$")) {
                String toReplace = entry.getValue();
                Optional<String> replacement = environment.getVariablesList().stream()
                        .filter(var -> toReplace.replace("$", "").equals(var.getName())) // Find environmental variable that matches docker parameter
                        .map(Protos.Environment.Variable::getValue)
                        .findFirst();
                if (replacement.isPresent()) {
                    dockerParameters.put(entry.getKey(), replacement.get());
                } else {
                    logger.warn("Docker parameter " + toReplace + " not found in environmental variables.");
                    dockerParameters.put(entry.getKey(), entry.getValue());
                }
            } else {
                dockerParameters.put(entry.getKey(), entry.getValue());
            }
        }
        return dockerParameters;
    }
}
