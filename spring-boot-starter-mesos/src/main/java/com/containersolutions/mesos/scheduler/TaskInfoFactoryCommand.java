package com.containersolutions.mesos.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class TaskInfoFactoryCommand implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${spring.application.name}")
    protected String applicationName;

    private final MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory;

    private final Supplier<UUID> uuidSupplier;

    public TaskInfoFactoryCommand(MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory, Supplier<UUID> uuidSupplier) {
        this.commandInfoMesosProtoFactory = commandInfoMesosProtoFactory;
        this.uuidSupplier = uuidSupplier;
    }

    @Override
    public Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources, ExecutionParameters executionParameters) {
        logger.debug("Creating Mesos task for taskId=" + taskId);
        return Protos.TaskInfo.newBuilder()
                .setName(applicationName + ".task")
                .setSlaveId(offer.getSlaveId())
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .addAllResources(resources)
                .setCommand(commandInfoMesosProtoFactory.create(executionParameters.getEnvironmentVariables()))
                .build();
    }
}
