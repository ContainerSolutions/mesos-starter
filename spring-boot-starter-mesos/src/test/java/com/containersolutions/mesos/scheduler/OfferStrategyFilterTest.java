package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.requirements.ResourceRequirement;
import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class OfferStrategyFilterTest {
    private Protos.Offer offer = TestHelper.createDummyOffer();

    @Mock
    private ResourceRequirement resourceRequirement;

    private final HashMap<String, ResourceRequirement> resourceRequirements = new HashMap<>();

    private OfferStrategyFilter filter = new OfferStrategyFilter(resourceRequirements);
    private String taskId = "taskId";

    @Before
    public void setUp() throws Exception {
        resourceRequirements.put("requirement 1", resourceRequirement);
    }

    @Test
    public void willApproveValidOffer() throws Exception {
        when(resourceRequirement.check("requirement 1", taskId, offer)).thenReturn(OfferEvaluation.accept("test requirement", taskId, offer, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList()));

        final OfferEvaluation result = filter.evaluate(taskId, offer);
        assertTrue(result.isValid());
        assertSame(offer, result.getOffer());
        verify(resourceRequirement).check("requirement 1", taskId, offer);
    }

    @Test
    public void willRejectInvalidOffer() throws Exception {
        when(resourceRequirement.check("requirement 1", taskId, offer)).thenReturn(OfferEvaluation.decline("test requirement", taskId, offer, null));
        assertFalse(filter.evaluate(taskId, offer).isValid());
        verify(resourceRequirement).check("requirement 1", taskId, offer);
    }

    @Test @Ignore("A nice to have that's not ready yet")
    public void willNotCheckSecondRequirementIfFirstRejects() throws Exception {
        ResourceRequirement decliningRequirement = mock(ResourceRequirement.class);
        resourceRequirements.put("requirement 2", decliningRequirement);

        when(resourceRequirement.check("requirement 1", taskId, offer)).thenReturn(OfferEvaluation.decline("requirement 1", taskId, offer, null));
        when(decliningRequirement.check("requirement 2", taskId, offer)).thenReturn(OfferEvaluation.decline("requirement 2", taskId, offer, null));

        assertFalse(filter.evaluate(taskId, offer).isValid());

        verifyZeroInteractions(decliningRequirement);
    }

    @Test
    public void willCheckSecondRequirementIfFirstApproves() throws Exception {
        ResourceRequirement approvingRequirement = mock(ResourceRequirement.class);
        resourceRequirements.put("requirement 2", approvingRequirement);

        when(resourceRequirement.check("requirement 1", taskId, offer)).thenReturn(OfferEvaluation.accept("requirement 1", taskId, offer, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList()));
        when(approvingRequirement.check("requirement 2", taskId, offer)).thenReturn(OfferEvaluation.accept("requirement 2", taskId, offer, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList()));

        assertTrue(filter.evaluate(taskId, offer).isValid());

        verify(approvingRequirement).check("requirement 2", taskId, offer);
    }

}