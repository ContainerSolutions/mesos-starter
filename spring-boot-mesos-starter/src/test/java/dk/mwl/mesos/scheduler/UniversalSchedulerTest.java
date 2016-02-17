package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.TestHelper;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;

import static dk.mwl.mesos.TestHelper.createDummyOffer;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UniversalSchedulerTest {
    @Mock
    OfferStrategyFilter offerStrategyFilter;

    @Mock
    TaskInfoFactory taskInfoFactory;

    @InjectMocks
    UniversalScheduler scheduler = new UniversalScheduler();

    @Mock
    SchedulerDriver schedulerDriver;

    @Test
    public void willDeclineInvalidOffers() throws Exception {
        Protos.Offer offer = createDummyOffer();

        when(offerStrategyFilter.evaluate(offer)).thenReturn(new OfferEvaluation(offer, false));

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver).declineOffer(offer.getId());
        verifyNoMoreInteractions(schedulerDriver);
    }

    @Test
    public void willLaunchTaskFromValidOffer() throws Exception {
        Protos.Offer offer = createDummyOffer();
        Protos.TaskInfo task = TestHelper.createDummyTask();

        when(offerStrategyFilter.evaluate(offer)).thenReturn(new OfferEvaluation(offer, true));
        when(taskInfoFactory.create(offer, Collections.emptyList())).thenReturn(task);

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver, never()).declineOffer(any(Protos.OfferID.class));
        verify(schedulerDriver).launchTasks(Collections.singleton(offer.getId()), Collections.singleton(task));
    }
}