package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;

import static com.containersolutions.mesos.TestHelper.createDummyOffer;
import static com.containersolutions.mesos.TestHelper.createDummyTask;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstancesCountRequirementTest {
    @Mock
    Clock clock;

    @Mock
    StateRepository stateRepository;

    @InjectMocks
    InstancesCountRequirement requirement = new InstancesCountRequirement();

    @Test
    public void willRejectOfferWhenCountIsReached() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        requirement.onApplicationEvent(createUpdate(Protos.TaskState.TASK_RUNNING, "taskId 1"));
        when(stateRepository.allTaskInfos()).thenReturn(Collections.singleton(TestHelper.createDummyTask("task")));

        assertFalse(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());
    }

    @Test
    public void willRemoveTentativeReservations() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        assertFalse(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());

        when(clock.instant()).thenReturn(Instant.now().plusSeconds(120));
        assertTrue(requirement.check("test requirement", "taskId 3", TestHelper.createDummyOffer()).isValid());
    }

    @Test
    public void willRemoveFailedTentativeReservations() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
        requirement.onApplicationEvent(createUpdate(Protos.TaskState.TASK_FAILED, "taskId 1"));

        assertTrue(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());
    }

    @Test
    public void willNotAcceptTooManyOffers() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());

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