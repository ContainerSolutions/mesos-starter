package dk.mwl.mesos.scheduler.requirements;

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
public class DistinctSlaveRequirementTest {
    @Mock
    Clock clock;

    @InjectMocks
    DistinctSlaveRequirement requirement = new DistinctSlaveRequirement();

    private String taskId = "taskId";
    private Instant now = Instant.now();

    @Test
    public void willRejectOffersForHostWithRunningTask() throws Exception {
        when(clock.instant()).thenReturn(now);
        assertTrue(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
        assertFalse(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
    }

    @Test
    public void willNotAcceptTwoOffersForSameSlave() throws Exception {
        when(clock.instant()).thenReturn(now);
        assertTrue(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
        assertFalse(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
    }

    @Test
    public void willAcceptOfferForTentativeHostAfterAWhile() throws Exception {
        when(clock.instant()).thenReturn(now);
        assertTrue(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());

        when(clock.instant()).thenReturn(now.plusSeconds(120));
        assertTrue(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
    }

    private Protos.Offer createOffer(String slave) {
        return createDummyOffer(builder -> builder.setSlaveId(Protos.SlaveID.newBuilder().setValue(slave)));
    }

    private StatusUpdateEvent createUpdate(Protos.TaskState taskState, String taskId, String slave) {
        return new StatusUpdateEvent(Protos.TaskStatus.newBuilder()
                .setState(taskState)
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(slave))
                .build()
        );
    }

}