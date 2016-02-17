package dk.mwl.mesos.config.autoconfigure;

import dk.mwl.mesos.scheduler.OfferEvaluation;
import dk.mwl.mesos.scheduler.ResourceRequirement;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.core.env.Environment;

import static dk.mwl.mesos.TestHelper.createDummyOffer;
import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MesosSchedulerConfigurationTest {

    @Mock
    Environment environment;

    @InjectMocks
    MesosSchedulerConfiguration configuration = new MesosSchedulerConfiguration();

    @Test
    public void willChoseSequentialPorts() throws Exception {
        when(environment.getRequiredProperty("mesos.resources.ports", Integer.class)).thenReturn(1);
        final ResourceRequirement requirement = configuration.portsRequirement();

        final OfferEvaluation ports = requirement.apply(createDummyOffer(builder -> {
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
        assertEquals(1000, ports.getResources().get(0).getRanges().getRange(0).getEnd());
    }
}