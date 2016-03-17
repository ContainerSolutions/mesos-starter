package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.InstanceCount;
import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstancesCountRequirementTest {
    @Mock
    Clock clock;

    @Mock
    StateRepository stateRepository;

    @Mock
    InstanceCount instanceCount;

    @InjectMocks
    InstancesCountRequirement requirement = new InstancesCountRequirement();

    @Test
    public void willRejectOfferWhenCountIsReached() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        when(instanceCount.getCount()).thenReturn(1);
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        requirement.onApplicationEvent(createUpdate(Protos.TaskState.TASK_RUNNING, "taskId 1"));
        when(stateRepository.allTaskInfos()).thenReturn(Collections.singleton(TestHelper.createDummyTask("task")));

        assertFalse(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());
    }

    @Test
    public void willRemoveTentativeReservations() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        when(instanceCount.getCount()).thenReturn(1);

        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        verify(instanceCount).incrementTentative();
        when(instanceCount.getTentative()).thenReturn(1);

        assertFalse(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());

        Mockito.clearInvocations(instanceCount);

        when(clock.instant()).thenReturn(Instant.now().plusSeconds(120));
        when(instanceCount.getTentative()).thenReturn(0);
        assertTrue(requirement.check("test requirement", "taskId 3", TestHelper.createDummyOffer()).isValid());
        verify(instanceCount).decrementTentative();
        verify(instanceCount).incrementTentative();
    }

    @Test
    public void willRemoveFailedTentativeReservations() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        when(instanceCount.getCount()).thenReturn(1);
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        requirement.onApplicationEvent(createUpdate(Protos.TaskState.TASK_FAILED, "taskId 1"));

        assertTrue(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());
    }

    @Test
    public void willNotAcceptTooManyOffers() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        when(instanceCount.getCount()).thenReturn(1);
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        verify(instanceCount).incrementTentative();

        when(instanceCount.getTentative()).thenReturn(1);
        assertFalse(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());
    }

    private StatusUpdateEvent createUpdate(Protos.TaskState taskState, String taskId) {
        return new StatusUpdateEvent(Protos.TaskStatus.newBuilder()
                .setState(taskState)
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .build()
        );
    }
}