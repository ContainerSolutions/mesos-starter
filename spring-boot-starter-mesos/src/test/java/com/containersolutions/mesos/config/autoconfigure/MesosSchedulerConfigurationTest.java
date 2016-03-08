package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.requirements.ResourceRequirement;
import com.containersolutions.mesos.scheduler.requirements.ports.PortParser;
import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static com.containersolutions.mesos.TestHelper.createDummyOffer;

@RunWith(MockitoJUnitRunner.class)
public class MesosSchedulerConfigurationTest {

    @InjectMocks
    MesosSchedulerConfiguration configuration = new MesosSchedulerConfiguration();

    @Mock
    PortParser portParser;

    @Mock
    MesosConfigProperties mesosConfig;

//    @Test
//    public void shouldAddEnumPorts() {
//        List<String> portList = new ArrayList<>();
//        portList.add("One");
//        portList.add("Two");
//        portList.add("Three");
//        portParser.getPorts();
//        PortParser.StarterPort mock = mock(PortParser.StarterPort.class);
//        when(mock.)
//
//        when(portParser.portsFor(any())).thenReturn(portList);
//        when(portParser.size()).thenReturn(portList.size() * ResourcesConfigProperties.PortType.values().length);
//        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig, portParser);
//        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
//            builder.addResources(Protos.Resource.newBuilder()
//                            .setName("ports")
//                            .setType(Protos.Value.Type.RANGES)
//                            .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
//            );
//        }));
//
//        assertTrue(ports.isValid());
//    }
//
//    @Test
//    public void shouldAddPrivilegedPorts() {
//        when(portParser.portsFor(eq(PRIVILEDGED))).thenReturn(Arrays.asList("One"));
//        when(portParser.size()).thenReturn(1);
//        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig, portParser);
//        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
//            builder.addResources(Protos.Resource.newBuilder()
//                            .setName("ports")
//                            .setType(Protos.Value.Type.RANGES)
//                            .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
//            );
//        }));
//
//        assertTrue(ports.isValid());
//        assertEquals(1000, ports.getResources().get(0).getRanges().getRange(0).getBegin());
//    }


    @Test
    public void shouldAddAnyPorts() {
        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig, portParser);
        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
            builder.addResources(Protos.Resource.newBuilder()
                            .setName("ports")
                            .setType(Protos.Value.Type.RANGES)
                            .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
            );
        }));
    }
//
//    @Test
//    public void shouldAddAnyPorts() {
//        when(portParser.portsFor(eq(ANY))).thenReturn(Collections.singletonList("One"));
//        when(portParser.size()).thenReturn(1);
//        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig, portParser);
//        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
//            builder.addResources(Protos.Resource.newBuilder()
//                            .setName("ports")
//                            .setType(Protos.Value.Type.RANGES)
//                            .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
//            );
//        }));
//
//        assertTrue(ports.isValid());
//        assertEquals(1024, ports.getResources().get(0).getRanges().getRange(0).getBegin());
//    }

//    @Test
//    public void shouldAddUnprivilegedPorts() {
//        when(portParser.portsFor(eq(UNPRIVILEDGED))).thenReturn(Collections.singletonList("One"));
//        when(portParser.size()).thenReturn(1);
//        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig, portParser);
//        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
//            builder.addResources(Protos.Resource.newBuilder()
//                            .setName("ports")
//                            .setType(Protos.Value.Type.RANGES)
//                            .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
//            );
//        }));
//
//        assertTrue(ports.isValid());
//        assertEquals(1024, ports.getResources().get(0).getRanges().getRange(0).getBegin());
//    }
//
//    @Test
//    public void shouldAddFixedPorts() throws Exception {
//        Map<String, Protos.Port> fixedPortMap = new HashMap<>();
//        fixedPortMap.put("One", Protos.Port.newBuilder().setNumber(1000).build());
//        fixedPortMap.put("Two", Protos.Port.newBuilder().setNumber(1001).build());
//        fixedPortMap.put("Three", Protos.Port.newBuilder().setNumber(1002).build());
//        when(portParser.fixedPorts()).thenReturn(fixedPortMap);
//        when(portParser.size()).thenReturn(fixedPortMap.size());
//        final ResourceRequirement requirement = configuration.portsRequirement(mesosConfig, portParser);
//        final OfferEvaluation ports = requirement.check("test", "taskId", createDummyOffer(builder -> {
//            builder.addResources(Protos.Resource.newBuilder()
//                    .setName("ports")
//                    .setType(Protos.Value.Type.RANGES)
//                    .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(1000).setEnd(2000)))
//            );
//            builder.addResources(Protos.Resource.newBuilder()
//                    .setName("ports")
//                    .setType(Protos.Value.Type.RANGES)
//                    .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(3000).setEnd(4000)))
//            );
//        }));
//
//        assertTrue(ports.isValid());
//        assertEquals(1000, ports.getResources().get(0).getRanges().getRange(0).getBegin());
//        assertEquals(1000, ports.getResources().get(0).getRanges().getRange(0).getEnd());
//        assertEquals(1001, ports.getResources().get(2).getRanges().getRange(0).getEnd());
//        assertEquals(1002, ports.getResources().get(4).getRanges().getRange(0).getEnd());
//    }
}