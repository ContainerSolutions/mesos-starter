package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.*;

import static com.containersolutions.mesos.config.autoconfigure.MesosSchedulerConfiguration.MESOS_PORTS;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anySet;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class PortPickerTest {
    @Mock
    PortParser portParser;

    @Mock
    PortMerger portMerger;

    @Mock
    List<PortPicker.PortResourceMapper> resourceMappers;


    @InjectMocks
    PortPicker portPicker;

    @Test
    public void shouldReturnValidPortMergerSaysValid() {
        when(portMerger.allPortsMerged(anySet(), anyList())).thenReturn(true);
        assertTrue(portPicker.isValid(Collections.EMPTY_SET));
    }

    @Test
    public void shouldReturnValidPortMergerSaysInvalid() {
        when(portMerger.allPortsMerged(anySet(), anyList())).thenReturn(false);
        assertFalse(portPicker.isValid(Collections.EMPTY_SET));
    }

    @Test
    public void shouldReturnResources() {
        when(portMerger.merge(any(), any())).thenReturn(Arrays.asList(Protos.Resource.newBuilder().setName(MESOS_PORTS).setType(Protos.Value.Type.TEXT).build()));
        assertEquals(1, portPicker.getResources(mock(Set.class)).size());
        verify(portParser, times(1)).getPorts();
    }

    @Test
    public void shouldReturnPortSetSize1() {
        Protos.Offer.Builder offer = defaultProto().addResources(defaultResource()
                .setRanges(Protos.Value.Ranges.newBuilder()
                        .addRange(Protos.Value.Range.newBuilder()
                                .setBegin(0)
                                .setEnd(0))));
        Set<Integer> ports = PortPicker.toPortSet(offer.build());
        assertEquals(1, ports.size());
        assertEquals(0, ports.iterator().next().intValue());
    }

    @Test
    public void shouldReturnPortSetSize2() {
        Protos.Offer.Builder offer = defaultProto()
                .addResources(defaultResource()
                        .setRanges(Protos.Value.Ranges.newBuilder()
                                .addRange(Protos.Value.Range.newBuilder()
                                        .setBegin(0)
                                        .setEnd(1))));
        Set<Integer> ports = PortPicker.toPortSet(offer.build());
        assertEquals(2, ports.size());
        Iterator<Integer> iterator = ports.iterator();
        assertEquals(0, iterator.next().intValue());
        assertEquals(1, iterator.next().intValue());
    }

    @Test
    public void shouldReturnPortSetDualRange() {
        Protos.Offer.Builder offer = defaultProto()
                .addResources(defaultResource()
                        .setRanges(Protos.Value.Ranges.newBuilder()
                                .addRange(Protos.Value.Range.newBuilder()
                                                .setBegin(0)
                                                .setEnd(0)
                                )
                                .addRange(Protos.Value.Range.newBuilder()
                                                .setBegin(1)
                                                .setEnd(1)
                                )));
        Set<Integer> ports = PortPicker.toPortSet(offer.build());
        assertEquals(2, ports.size());
        Iterator<Integer> iterator = ports.iterator();
        assertEquals(0, iterator.next().intValue());
        assertEquals(1, iterator.next().intValue());
    }

    @Test
    public void shouldReturnEmpty() {
        Protos.Offer.Builder offer = defaultProto()
                .addResources(defaultResource());
        Set<Integer> ports = PortPicker.toPortSet(offer.build());
        assertEquals(0, ports.size());
    }

    private Protos.Resource.Builder defaultResource() {
        return Protos.Resource.newBuilder()
                .setName(MESOS_PORTS)
                .setType(Protos.Value.Type.RANGES);
    }

    private Protos.Offer.Builder defaultProto() {
        return Protos.Offer.newBuilder()
                .setId(Protos.OfferID.newBuilder().setValue(""))
                .setFrameworkId(Protos.FrameworkID.newBuilder().setValue(""))
                .setSlaveId(Protos.SlaveID.newBuilder().setValue(""))
                .setHostname("");
    }
}