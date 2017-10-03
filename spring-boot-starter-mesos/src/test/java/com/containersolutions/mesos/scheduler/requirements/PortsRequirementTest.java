package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcePortConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.apache.mesos.Protos;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static com.containersolutions.mesos.TestHelper.createDummyOffer;
import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class PortsRequirementTest {

    private MesosConfigProperties mesosConfig = new MesosConfigProperties();

    private PortsRequirement requirement = new PortsRequirement(mesosConfig);


    @Before
    public void setUp() throws Exception {
        mesosConfig.setResources(new ResourcesConfigProperties());
    }

    private ResourcePortConfigProperties createPort(String hostPort) {
        ResourcePortConfigProperties port = new ResourcePortConfigProperties();
        port.setHost(hostPort);
        return port;

    }

    @Test
    public void willChooseDesiredPorts() throws Exception {
        mesosConfig.getResources().getPorts().put("a", createPort("PRIVILEGED"));
        mesosConfig.getResources().getPorts().put("b", createPort("UNPRIVILEGED"));
        mesosConfig.getResources().getPorts().put("c", createPort("ANY"));
        mesosConfig.getResources().getPorts().put("d", createPort("1005"));
        mesosConfig.getResources().getPorts().put("e", createPort("3333"));

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
        assertEquals(5, ports.getEnvironmentVariables().size());
        assertEquals(1025, ports.getResources().get(0).getRanges().getRange(2).getBegin());
        assertEquals("1000", ports.getEnvironmentVariables().get("A"));
        assertEquals("1025", ports.getEnvironmentVariables().get("B"));
        assertEquals("1026", ports.getEnvironmentVariables().get("C"));
        assertEquals("1005", ports.getEnvironmentVariables().get("D"));
        assertEquals("3333", ports.getEnvironmentVariables().get("E"));
    }

    @Test
    public void willRejectIfFixedPortIsMissing() throws Exception {
        mesosConfig.getResources().getPorts().put("a", createPort("9999"));

        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> builder.addResources(Protos.Resource.newBuilder()
                .setName("ports")
                .setType(Protos.Value.Type.RANGES)
                .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
        )));

        assertFalse(ports.isValid());
    }

    @Test
    public void testwillMapANYPortToFirstUnprivilegedPort() throws Exception {
        mesosConfig.getResources().getPorts().put("first", createPort("ANY"));

        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> builder.addResources(Protos.Resource.newBuilder()
                .setName("ports")
                .setType(Protos.Value.Type.RANGES)
                .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
        )));
        assertTrue(ports.isValid());
        assertEquals(1025, ports.getResources().get(0).getRanges().getRange(0).getBegin());

    }

    @Test
    public void testwillMapFixedPortToCorrectPort() throws Exception {
        mesosConfig.getResources().getPorts().put("first", createPort("1024"));

        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> builder.addResources(Protos.Resource.newBuilder()
                .setName("ports")
                .setType(Protos.Value.Type.RANGES)
                .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
        )));
        assertTrue(ports.isValid());
        assertEquals(1024, ports.getResources().get(0).getRanges().getRange(0).getBegin());

    }

}