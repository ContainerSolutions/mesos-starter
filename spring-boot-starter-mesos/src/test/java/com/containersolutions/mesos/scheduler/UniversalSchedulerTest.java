package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UniversalSchedulerTest {
    @InjectMocks
    UniversalScheduler scheduler = new UniversalScheduler();

    @Mock
    private SchedulerDriver schedulerDriver;

    @Mock
    private StateRepository stateRepository;

    @Mock
    private VirtualOfferFactory offerSlicer;

    @Mock
    private TaskDescriptionFactory taskDescriptionFactory;

    @Test
    public void willDeclineOffersWithInsufficientResources() throws Exception {
        Protos.Offer offer = TestHelper.createDummyOffer(0.1, 1.0);

        when(offerSlicer.slice(offer)).thenReturn(Stream.empty());

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver).declineOffer(offer.getId());
        verifyNoMoreInteractions(schedulerDriver);
    }

    @Test
    public void willLaunchTaskFromValidOffer() throws Exception {
        Protos.Offer offer = TestHelper.createDummyOffer(2.0, 2048.0);
        Protos.TaskInfo taskInfo = TestHelper.createDummyTask("task", builder -> builder.setTaskId(Protos.TaskID.newBuilder().setValue("TaskId")));
        VirtualOffer vOffer = new VirtualOffer(offer);

        when(offerSlicer.slice(offer)).thenReturn(Stream.of(vOffer));
        when(taskDescriptionFactory.create(vOffer)).thenReturn(new TaskDescription(vOffer, taskInfo));

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verify(schedulerDriver, never()).declineOffer(any(Protos.OfferID.class));
        //noinspection ArraysAsListWithZeroOrOneArgument
        verify(schedulerDriver).launchTasks(Collections.singleton(offer.getId()), asList(taskInfo));

/*
        ArgumentCaptor<Protos.TaskInfo> taskInfoArgumentCaptor = ArgumentCaptor.forClass(Protos.TaskInfo.class);
        verify(stateRepository).store(taskInfoArgumentCaptor.capture());
        assertSame(taskInfo, taskInfoArgumentCaptor.getValue());
*/
    }

    @Test
    public void willDeclineOfferIfOfferRequirementRejects() throws Exception {
        Protos.Offer offer = TestHelper.createDummyOffer(2.0, 2048.0);
        OfferRequirement offerRequirement = mock(OfferRequirement.class);
        scheduler.offerRequirements = Collections.singletonList(offerRequirement);

        when(offerRequirement.check(offer)).thenReturn(false);

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verifyZeroInteractions(offerSlicer);
        verify(schedulerDriver).declineOffer(offer.getId());
        verifyNoMoreInteractions(schedulerDriver);


    }

    @Test
    public void willDeclineOfferIfVirtualOfferRequirementRejects() throws Exception {
        Protos.Offer offer = TestHelper.createDummyOffer(2.0, 2048.0);
        VirtualOffer vOffer = new VirtualOffer(offer);
        VirtualOfferRequirement virtualOfferRequirement = mock(VirtualOfferRequirement.class);
        scheduler.virtualOfferRequirements = Collections.singletonList(virtualOfferRequirement);

        when(offerSlicer.slice(offer)).thenReturn(Stream.of(vOffer));
        when(virtualOfferRequirement.check(vOffer)).thenReturn(false);

        scheduler.resourceOffers(schedulerDriver, Collections.singletonList(offer));

        verifyZeroInteractions(taskDescriptionFactory);
        verify(schedulerDriver).declineOffer(offer.getId());
        verifyNoMoreInteractions(schedulerDriver);
    }

    @Test
    public void canAcceptOnlyTwoTask() throws Exception {
        Protos.Offer offer1 = TestHelper.createDummyOffer(2.0, 2048.0);
        Protos.Offer offer2 = TestHelper.createDummyOffer(2.0, 2048.0);
        Protos.Offer offer3 = TestHelper.createDummyOffer(2.0, 2048.0);


        when(offerSlicer.slice(any(Protos.Offer.class))).thenAnswer(invocation -> Stream.of(new VirtualOffer(((Protos.Offer) invocation.getArguments()[0]))));
        when(taskDescriptionFactory.create(any(VirtualOffer.class))).thenAnswer(invocation -> new TaskDescription(((VirtualOffer) invocation.getArguments()[0]), TestHelper.createDummyTask(RandomStringUtils.randomAlphanumeric(10))));

        List<TaskDescription> storedTaskDescriptions = new ArrayList<>();
        scheduler.virtualOfferRequirements = Collections.singletonList(virtualOffer -> storedTaskDescriptions.size() < 2);
        doAnswer(invocation -> {
            System.out.println("Storing " + ((TaskDescription) invocation.getArguments()[0]).getTaskInfo().getName());
            return storedTaskDescriptions.add(((TaskDescription) invocation.getArguments()[0]));
        }).when(stateRepository).store(any(TaskDescription.class));

        scheduler.resourceOffers(schedulerDriver, asList(offer1, offer2, offer3));

        verify(schedulerDriver).declineOffer(offer3.getId());
        verify(schedulerDriver).launchTasks(eq(Collections.singleton(offer1.getId())), anyListOf(Protos.TaskInfo.class));
        verify(schedulerDriver).launchTasks(eq(Collections.singleton(offer2.getId())), anyListOf(Protos.TaskInfo.class));
    }
}