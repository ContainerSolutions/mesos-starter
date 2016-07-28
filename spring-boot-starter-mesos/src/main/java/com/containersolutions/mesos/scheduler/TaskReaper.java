package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.events.InstanceCountChangeEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.util.Set;

public class TaskReaper implements ApplicationListener<InstanceCountChangeEvent> {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    StateRepository stateRepository;

    @Autowired
    InstanceCount instanceCount;

    @Autowired
    UniversalScheduler universalScheduler;

    @Override
    public void onApplicationEvent(InstanceCountChangeEvent event) {
        int expectedInstances = instanceCount.getCount();
        Set<TaskDescription> taskInfos = stateRepository.allTaskDescriptions();
        int runningInstances = taskInfos.size();

        int rest = runningInstances - expectedInstances;
        taskInfos.stream()
                .limit(Math.max(0, rest))
                .map(TaskDescription::getTaskId)
                .peek(taskId -> logger.info("Killing taskId=" + taskId))
                .map(id -> Protos.TaskID.newBuilder().setValue(id).build())
                .forEach(universalScheduler::killTask);

    }
}
