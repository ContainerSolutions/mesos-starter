package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.requirements.ResourceRequirement;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

import static com.containersolutions.mesos.TestHelper.createDummyOffer;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class MesosSchedulerConfigurationTest {

    @InjectMocks
    MesosSchedulerConfiguration configuration = new MesosSchedulerConfiguration();

    @Test
    public void willChoseDesiredPorts() throws Exception {
        MesosConfigProperties mesosConfig = new MesosConfigProperties();
        ResourcesConfigProperties resources = new ResourcesConfigProperties();
        resources.setPort(asList("1", "2", "3"));
        mesosConfig.setResources(resources);
        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig);

        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
            builder.addResources(Protos.Resource.newBuilder()
                    .setName("ports")
                    .setType(Protos.Value.Type.RANGES)
                    .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
            );
            builder.addResources(Protos.Resource.newBuilder()
                    .setName("ports")
                    .setType(Protos.Value.Type.RANGES)
                    .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(3000).setEnd(4000)))
            );
        }));

        assertTrue(ports.isValid());
        assertEquals(1000, ports.getResources().get(0).getRanges().getRange(0).getBegin());
        assertEquals(1002, ports.getResources().get(0).getRanges().getRange(0).getEnd());
    }
}