package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

@RunWith(MockitoJUnitRunner.class)
public class PortMergerTest {
    List<PortPicker.PortResourceMapper> resourceMappers = Arrays.asList(port -> PortUtil.defaultResource(port.getNumber()).build());

    PortMerger portMerger = new PortMerger(resourceMappers);

    @Test
    public void shouldReturnTrueWhenRequestedAndResultPortsSameSize() {
        HashSet<Integer> offeredPorts = new HashSet<>();
        offeredPorts.add(1);
        List<PortFactory.PortFunction> requestedPorts = Arrays.asList((PortFactory.PortFunction) integers -> Arrays.asList(validPort(1)));
        assertTrue(portMerger.allPortsMerged(offeredPorts, requestedPorts));
    }

    private Protos.Port validPort(Integer port) {
        return Protos.Port.newBuilder().setName("test").setNumber(port).build();
    }

    @Test
    public void shouldReturnFlaseWhenRequestedAndResultPortsNotSameSize() {
        HashSet<Integer> offeredPorts = new HashSet<>();
        List<PortFactory.PortFunction> requestedPorts = Arrays.asList((PortFactory.PortFunction) integers -> Collections.emptyList());
        assertFalse(portMerger.allPortsMerged(offeredPorts, requestedPorts));
    }

    @Test
    public void shouldMerge1Port() {
        HashSet<Integer> offeredPorts = new HashSet<>();
        offeredPorts.add(1);
        List<PortFactory.PortFunction> requestedPorts = Arrays.asList((PortFactory.PortFunction) integers -> Arrays.asList(validPort(1)));
        List<Protos.Resource> resources = portMerger.merge(offeredPorts, requestedPorts);
        assertEquals(1, resources.size());
        assertTrue(resources.toString().contains("1"));
    }

    @Test
    public void shouldMerge2Ports() {
        HashSet<Integer> offeredPorts = new HashSet<>();
        offeredPorts.add(0);
        offeredPorts.add(1);
        List<PortFactory.PortFunction> requestedPorts = Arrays.asList((PortFactory.PortFunction) integers -> Arrays.asList(validPort(0), validPort(1)));
        List<Protos.Resource> resources = portMerger.merge(offeredPorts, requestedPorts);
        assertEquals(2, resources.size());
        assertTrue(resources.toString().contains("0"));
        assertTrue(resources.toString().contains("1"));
    }
}