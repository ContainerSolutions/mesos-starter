package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.InstanceCount;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstancesCountRequirementTest {
    private StateRepository stateRepository = mock(StateRepository.class);

    private InstanceCount instanceCount = mock(InstanceCount.class);

    private InstancesCountRequirement requirement = new InstancesCountRequirement(stateRepository, instanceCount);

    @Test
    public void willAcceptOfferWhenCountIsNotReached() throws Exception {
        when(instanceCount.getCount()).thenReturn(1);
        when(stateRepository.allTaskInfos()).thenReturn(Collections.emptySet());
        assertTrue(requirement.check("test requirement", "taskId 1", TestHelper.createDummyOffer()).isValid());
    }

    @Test
    public void willRejectOfferWhenCountIsReached() throws Exception {
        when(instanceCount.getCount()).thenReturn(1);
        when(stateRepository.allTaskInfos()).thenReturn(Collections.singleton(TestHelper.createDummyTask("task")));
        assertFalse(requirement.check("test requirement", "taskId 2", TestHelper.createDummyOffer()).isValid());
    }
}