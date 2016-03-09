package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.requirements.ResourceRequirement;
import com.containersolutions.mesos.scheduler.requirements.ports.PortPicker;
import com.containersolutions.mesos.scheduler.requirements.ports.PortUtil;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MesosSchedulerConfigurationTest {

    @InjectMocks
    MesosSchedulerConfiguration configuration = new MesosSchedulerConfiguration();

    @Mock
    PortPicker portPicker;

    @Test // Can't test that the list exists because that is a Spring thing. Doesn't get autowired in MockitoJUnitRunner
    public void shouldHaveResourceMappers() {
        assertTrue(configuration.mesosResourceMapper() != null);
    }

    @Test
    public void shouldReturnValidPortsRequirement() {
        when(portPicker.isValid(any())).thenReturn(true);
        List resources = mock(List.class);
        when(portPicker.getResources(any())).thenReturn(resources);
        ResourceRequirement resourceRequirement = configuration.portsRequirement(portPicker);

        Protos.Offer offer = PortUtil.defaultOffer().addResources(PortUtil.defaultResource()).build();
        OfferEvaluation evaluation = resourceRequirement.check("ports", "taskId", offer);
        assertTrue(evaluation.isValid());
        assertEquals(resources, evaluation.getResources());
        assertEquals("ports", evaluation.getRequirement());
    }
}