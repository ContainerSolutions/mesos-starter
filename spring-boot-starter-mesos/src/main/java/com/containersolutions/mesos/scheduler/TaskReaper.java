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

    private final StateRepository stateRepository;
    private final InstanceCount instanceCount;
    private final UniversalScheduler universalScheduler;

    public TaskReaper(StateRepository stateRepository, InstanceCount instanceCount, UniversalScheduler universalScheduler) {
        this.stateRepository = stateRepository;
        this.instanceCount = instanceCount;
        this.universalScheduler = universalScheduler;
    }

    @Override
    public void onApplicationEvent(InstanceCountChangeEvent event) {
        int expectedInstances = instanceCount.getCount();
        Set<Protos.TaskInfo> taskInfos = stateRepository.allTaskInfos();
        int runningInstances = taskInfos.size();

        int rest = runningInstances - expectedInstances;
        taskInfos.stream()
                .limit(Math.max(0, rest))
                .map(Protos.TaskInfo::getTaskId)
                .peek(taskId -> logger.info("Killing taskId=" + taskId.getValue()))
                .forEach(universalScheduler::killTask);

    }
}
