package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;

import static dk.mwl.mesos.TestHelper.createDummyOffer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ScaleFactorRequirementTest {
    @Mock
    Clock clock;

    @InjectMocks
    ScaleFactorRequirement requirement = new ScaleFactorRequirement(1);

    @Test
    public void willRejectOfferWhenScaleFactorReached() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", createDummyOffer()).isValid());
        requirement.onApplicationEvent(createUpdate(Protos.TaskState.TASK_RUNNING, "taskId 1"));

        assertFalse(requirement.check("test requirement", "taskId 2", createDummyOffer()).isValid());
    }

    @Test
    public void willRemoveTentativeReservations() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", createDummyOffer()).isValid());
        assertFalse(requirement.check("test requirement", "taskId 2", createDummyOffer()).isValid());

        when(clock.instant()).thenReturn(Instant.now().plusSeconds(120));
        assertTrue(requirement.check("test requirement", "taskId 3", createDummyOffer()).isValid());
    }

    @Test
    public void willRemoveFailedTentativeReservations() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", createDummyOffer()).isValid());
        requirement.onApplicationEvent(createUpdate(Protos.TaskState.TASK_FAILED, "taskId 1"));

        assertTrue(requirement.check("test requirement", "taskId 2", createDummyOffer()).isValid());
    }

    @Test
    public void willNotAcceptTooManyOffers() throws Exception {
        when(clock.instant()).thenReturn(Instant.now());
        assertTrue(requirement.check("test requirement", "taskId 1", createDummyOffer()).isValid());

        assertFalse(requirement.check("test requirement", "taskId 2", createDummyOffer()).isValid());
    }

    private StatusUpdateEvent createUpdate(Protos.TaskState taskState, String taskId) {
        return new StatusUpdateEvent(Protos.TaskStatus.newBuilder()
                .setState(taskState)
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .build()
        );
    }
}