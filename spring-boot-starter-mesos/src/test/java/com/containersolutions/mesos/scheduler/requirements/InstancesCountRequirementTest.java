package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.InstanceCount;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InstancesCountRequirementTest {
    @Mock
    StateRepository stateRepository;

    @Mock
    InstanceCount instanceCount;

    @InjectMocks
    InstancesCountRequirement requirement = new InstancesCountRequirement();

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