package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.events.InstanceCountChangeEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TaskReaperTest {
    private InstanceCount instanceCount = mock(InstanceCount.class);

    private StateRepository stateRepository = mock(StateRepository.class);

    private UniversalScheduler universalScheduler = mock(UniversalScheduler.class);

    private TaskReaper taskReaper = new TaskReaper(stateRepository, instanceCount, universalScheduler);

    @Test
    public void willNotKillTasksWhenCountIsFullfilled() throws Exception {
        when(stateRepository.allTaskInfos()).thenReturn(tasksInfoSet("task 1", "task 2"));
        when(instanceCount.getCount()).thenReturn(2);
        taskReaper.onApplicationEvent(new InstanceCountChangeEvent(1));

        verifyZeroInteractions(universalScheduler);
    }

    @Test
    public void willKillTaskWhenScalingDown() throws Exception {
        when(stateRepository.allTaskInfos()).thenReturn(tasksInfoSet("task 1", "task 2"));
        when(instanceCount.getCount()).thenReturn(1);
        taskReaper.onApplicationEvent(new InstanceCountChangeEvent(1));

        verify(universalScheduler).killTask(any(Protos.TaskID.class));
    }

    private static Set<Protos.TaskInfo> tasksInfoSet(String ... names) {
        return Arrays.stream(names)
                .map(TestHelper::createDummyTask)
                .collect(Collectors.toSet());
    }
}