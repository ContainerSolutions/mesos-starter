package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.TestHelper;
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
import static org.junit.Assert.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DistinctSlaveRequirementTest {
    @Mock
    Clock clock;

    @Mock
    StateRepository stateRepository;

    @InjectMocks
    DistinctSlaveRequirement requirement = new DistinctSlaveRequirement();

    private String taskId = "taskId";
    private Instant now = Instant.now();

    @Test
    public void willRejectOffersForHostWithTentativeRunningTask() throws Exception {
        when(clock.instant()).thenReturn(now);
        assertTrue(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
        assertFalse(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
    }

    @Test
    public void willNotAcceptTwoTentativeOffersForSameSlave() throws Exception {
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

    @Test
    public void willRejectOfferForHostWithRunningTask() {
        when(stateRepository.allTaskInfos()).thenReturn(Collections.singleton(createTaskInfo("slave 1", taskId)));

        assertFalse(requirement.check("test requirement", taskId, createOffer("slave 1")).isValid());
    }

    private Protos.TaskInfo createTaskInfo(String slave, String taskId) {
        return Protos.TaskInfo.newBuilder()
                .setName("test")
                .setTaskId(Protos.TaskID.newBuilder().setValue(taskId))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(slave))
                .build();
    }

    private Protos.Offer createOffer(String slave) {
        return TestHelper.createDummyOffer(builder -> builder.setSlaveId(Protos.SlaveID.newBuilder().setValue(slave)));
    }
}