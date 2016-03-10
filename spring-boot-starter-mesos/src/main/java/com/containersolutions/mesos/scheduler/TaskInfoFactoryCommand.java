package com.containersolutions.mesos.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

public class TaskInfoFactoryCommand implements TaskInfoFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired
    MesosProtoFactory<Protos.CommandInfo.Builder> commandInfoMesosProtoFactory;

    @Override
    public Protos.TaskInfo create(String taskId, Protos.Offer offer, List<Protos.Resource> resources) {
        logger.debug("Creating Mesos task for taskId=" + taskId);
        // Remove ports_env from actual mesos resources. They don't exist.
        List<Protos.Resource> mesosResources = resources.stream().filter(resource -> !resource.getName().equals("ports_env")).collect(Collectors.toList());
        Protos.TaskInfo taskInfo = Protos.TaskInfo.newBuilder()
                .setName(applicationName + ".task")
                .setSlaveId(offer.getSlaveId())
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .addAllResources(mesosResources)
                .setCommand(commandInfoMesosProtoFactory.create(resources))
                .build();
        logger.debug(taskInfo);
        return taskInfo;
    }
}
