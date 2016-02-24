package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.TestHelper;
import dk.mwl.mesos.scheduler.requirements.OfferEvaluation;
import dk.mwl.mesos.scheduler.state.StateRepository;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.UUID;
import java.util.function.Supplier;

import static dk.mwl.mesos.TestHelper.createDummyOffer;
import static org.junit.Assert.assertEquals;
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

    @Mock
    Supplier<UUID> uuidSupplier;

    @Mock
    StateRepository stateRepository;

    @Mock
    TaskMaterializer taskMaterializer;

    UUID uuid = UUID.randomUUID();
    String taskId = uuid.toString();

    @Test
    public void willDeclineInvalidOffers() throws Exception {
        Protos.Offer offer = createDummyOffer();

        when(uuidSupplier.get()).thenReturn(uuid);
        when(offerStrategyFilter.evaluate(taskId, offer)).thenReturn(new OfferEvaluation("test", taskId, offer, false));

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver).declineOffer(offer.getId());
        verifyNoMoreInteractions(schedulerDriver);
    }

    @Test
    public void willLaunchTaskFromValidOffer() throws Exception {
        Protos.Offer offer = createDummyOffer();
        Protos.TaskInfo task = TestHelper.createDummyTask(builder -> builder.setTaskId(Protos.TaskID.newBuilder().setValue(taskId)));

        when(uuidSupplier.get()).thenReturn(uuid);
        OfferEvaluation offerEvaluation = new OfferEvaluation("test", taskId, offer, true);
        when(offerStrategyFilter.evaluate(taskId, offer)).thenReturn(offerEvaluation);
        when(taskMaterializer.createProposal(offerEvaluation)).thenReturn(new TaskProposal(offer, task));
        when(taskInfoFactory.create(taskId, offer, Collections.emptyList())).thenReturn(task);

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver, never()).declineOffer(any(Protos.OfferID.class));
        verify(schedulerDriver).launchTasks(Collections.singleton(offer.getId()), Collections.singleton(task));

        ArgumentCaptor<Protos.TaskInfo> taskInfoArgumentCaptor = ArgumentCaptor.forClass(Protos.TaskInfo.class);
        verify(stateRepository).store(taskInfoArgumentCaptor.capture());
        Protos.TaskInfo taskInfo = taskInfoArgumentCaptor.getValue();
        assertEquals(taskId, taskInfo.getTaskId().getValue());
    }
}