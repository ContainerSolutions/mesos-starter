package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.mesos.Protos;
import org.apache.mesos.SchedulerDriver;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.*;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(UniversalSchedulerComponentTest.TestConfiguration.class)
public class UniversalSchedulerComponentTest {
    @Autowired
    UniversalScheduler scheduler;

    private final SchedulerDriver schedulerDriver = mock(SchedulerDriver.class);
    private final ArgumentCaptor<Collection<Protos.OfferID>> offerIdsCaptor = ArgumentCaptor.forClass(Collection.class);
    private final ArgumentCaptor<Collection<Protos.TaskInfo>> taskInfosCaptor = ArgumentCaptor.forClass(Collection.class);

    @Configuration
    @SpringBootApplication
    public static class TestConfiguration {
        @Bean
        public StateRepository stateRepositoryMemory() {
            return new StateRepository() {
                @Override
                public Optional<Protos.FrameworkID> getFrameworkID() {
                    return Optional.empty();
                }

                @Override
                public void store(Protos.TaskInfo taskInfo) {

                }

                @Override
                public Set<Protos.TaskInfo> allTaskInfos() {
                    return Collections.emptySet();
                }
            };
        }

        @Bean
        public UniversalScheduler universalScheduler() {
            return new UniversalScheduler() {
                @Override
                public void start() {
                    //Ignore start, as it'll try connect to Mesos master
                }
            };
        }
    }


    @Test
    public void willLaunchTaskFromOffer() throws Exception {
        scheduler.resourceOffers(schedulerDriver, singletonList(TestHelper.createDummyOffer(builder -> builder
                .setId(Protos.OfferID.newBuilder().setValue("ID 1"))
                .addResources(Protos.Resource.newBuilder().setName("cpus").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(1.0)))
                .addResources(Protos.Resource.newBuilder().setName("mem").setType(Protos.Value.Type.SCALAR).setScalar(Protos.Value.Scalar.newBuilder().setValue(1024))))));

        verify(schedulerDriver).launchTasks(offerIdsCaptor.capture(), taskInfosCaptor.capture());

        List<Protos.OfferID> offerIds = new ArrayList<>(offerIdsCaptor.getValue());
        ArrayList<Protos.TaskInfo> taskInfos = new ArrayList<>(taskInfosCaptor.getValue());
        assertEquals(1, offerIds.size());
        assertEquals("ID 1", offerIds.get(0).getValue());
        assertEquals(1, taskInfos.size());
    }


}