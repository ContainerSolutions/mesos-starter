package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.state.StateRepository;
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

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UniversalSchedulerTest {
    private OfferStrategyFilter offerStrategyFilter = mock(OfferStrategyFilter.class);

    private TaskInfoFactory taskInfoFactory = mock(TaskInfoFactory.class);

    private SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);

    private Supplier<UUID> uuidSupplier = mock(Supplier.class);

    private StateRepository stateRepository = mock(StateRepository.class);

    private TaskMaterializer taskMaterializer = mock(TaskMaterializer.class);

    private UniversalScheduler scheduler = new UniversalScheduler(null, offerStrategyFilter, null, uuidSupplier, stateRepository, taskMaterializer, null, null);

    private UUID uuid = UUID.randomUUID();
    private String taskId = uuid.toString();

    @Test
    public void willDeclineInvalidOffers() throws Exception {
        Protos.Offer offer = TestHelper.createDummyOffer();

        when(uuidSupplier.get()).thenReturn(uuid);
        when(offerStrategyFilter.evaluate(taskId, offer)).thenReturn(OfferEvaluation.decline("test", taskId, offer, null));

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver).declineOffer(offer.getId());
        verifyNoMoreInteractions(schedulerDriver);
    }

    @Test
    public void willLaunchTaskFromValidOffer() throws Exception {
        Protos.Offer offer = TestHelper.createDummyOffer();
        Protos.TaskInfo task = TestHelper.createDummyTask("task", builder -> builder.setTaskId(Protos.TaskID.newBuilder().setValue(taskId)));

        when(uuidSupplier.get()).thenReturn(uuid);
        OfferEvaluation offerEvaluation = OfferEvaluation.accept("test", taskId, offer, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        when(offerStrategyFilter.evaluate(taskId, offer)).thenReturn(offerEvaluation);
        when(taskMaterializer.createProposal(offerEvaluation)).thenReturn(new TaskProposal(offer, task));
        when(taskInfoFactory.create(taskId, offer, Collections.emptyList(), new ExecutionParameters(Collections.emptyMap(), Collections.emptyList(), Collections.emptyList()))).thenReturn(task);

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver, never()).declineOffer(any(Protos.OfferID.class));
        verify(schedulerDriver).launchTasks(Collections.singleton(offer.getId()), Collections.singleton(task));

        ArgumentCaptor<Protos.TaskInfo> taskInfoArgumentCaptor = ArgumentCaptor.forClass(Protos.TaskInfo.class);
        verify(stateRepository).store(taskInfoArgumentCaptor.capture());
        Protos.TaskInfo taskInfo = taskInfoArgumentCaptor.getValue();
        assertEquals(taskId, taskInfo.getTaskId().getValue());
    }
}