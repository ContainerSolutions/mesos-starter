package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.TestHelper;
import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;
import java.util.stream.Collectors;

import static com.containersolutions.mesos.TestHelper.createDummyOffer;
import static org.junit.Assert.assertEquals;

@RunWith(MockitoJUnitRunner.class)
public class VirtualOfferFactoryPackedTest {
    private final MesosConfigProperties configuration = new MesosConfigProperties();
    @InjectMocks
    VirtualOfferFactoryPacked factory = new VirtualOfferFactoryPacked(configuration);

    @Before
    public void setUp() throws Exception {
        ResourcesConfigProperties resources = new ResourcesConfigProperties();
        resources.setCpus(0.2);
        resources.setMem(8);
        configuration.setResources(resources);

    }

    @Test
    public void willUseCpuAsLimit() throws Exception {
        List<VirtualOffer> virtualOffers = factory.slice(TestHelper.createDummyOffer(1.0, 1024)).collect(Collectors.toList());
        assertEquals(5, virtualOffers.size());
    }

    @Test
    public void willUseCpuAndMemoryAsLimit() throws Exception {
        List<VirtualOffer> virtualOffers = factory.slice(TestHelper.createDummyOffer(12.0, 40)).collect(Collectors.toList());
        assertEquals(5, virtualOffers.size());
    }

}