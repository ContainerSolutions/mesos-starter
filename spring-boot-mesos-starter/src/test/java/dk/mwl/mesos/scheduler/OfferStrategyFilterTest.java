package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.TestHelper;
import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;


@RunWith(MockitoJUnitRunner.class)
public class OfferStrategyFilterTest {
    Protos.Offer offer = TestHelper.createDummyOffer();

    @Mock
    ResourceRequirement resourceRequirement;

    OfferStrategyFilter filter = new OfferStrategyFilter();
    private String taskId = "taskId";

    @Before
    public void setUp() throws Exception {
        filter.resourceRequirements = new ArrayList<>();
        filter.resourceRequirements.add(resourceRequirement);
    }

    @Test
    public void willApproveValidOffer() throws Exception {
        when(resourceRequirement.check(taskId, offer)).thenReturn(new OfferEvaluation(taskId, offer, true));

        final OfferEvaluation result = filter.evaluate(taskId, offer);
        assertTrue(result.isValid());
        assertSame(offer, result.getOffer());
        verify(resourceRequirement).check(taskId, offer);
    }

    @Test
    public void willRejectInvalidOffer() throws Exception {
        when(resourceRequirement.check(taskId, offer)).thenReturn(new OfferEvaluation(taskId, offer, false));
        assertFalse(filter.evaluate(taskId, offer).isValid());
        verify(resourceRequirement).check(taskId, offer);
    }

    @Test @Ignore("A nice to have that's not ready yet")
    public void willNotCheckSecondRequirementIfFirstRejects() throws Exception {
        ResourceRequirement decliningRequirement = mock(ResourceRequirement.class);
        filter.resourceRequirements.add(decliningRequirement);

        when(resourceRequirement.check(taskId, offer)).thenReturn(new OfferEvaluation(taskId, offer, false));
        when(decliningRequirement.check(taskId, offer)).thenReturn(new OfferEvaluation(taskId, offer, false));

        assertFalse(filter.evaluate(taskId, offer).isValid());

        verifyZeroInteractions(decliningRequirement);
    }

    @Test
    public void willCheckSecondRequirementIfFirstApproves() throws Exception {
        ResourceRequirement approvingRequirement = mock(ResourceRequirement.class);
        filter.resourceRequirements.add(approvingRequirement);

        when(resourceRequirement.check(taskId, offer)).thenReturn(new OfferEvaluation(taskId, offer, true));
        when(approvingRequirement.check(taskId, offer)).thenReturn(new OfferEvaluation(taskId, offer, true));

        assertTrue(filter.evaluate(taskId, offer).isValid());

        verify(approvingRequirement).check(taskId, offer);
    }

}