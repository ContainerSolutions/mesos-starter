package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


@RunWith(MockitoJUnitRunner.class)
public class PrivilegedPortTest {

    private String name = "test";
    private PrivilegedPort privilegedPort = new PrivilegedPort(name);

    @Test
    public void shouldShouldTakePrivilegedPort() {
        Integer port = 0;
        List<Protos.Port> ports = privilegedPort.apply(new HashSet<>(Collections.singletonList(port)));
        assertEquals(name, ports.get(0).getName());
        assertEquals(port.intValue(), ports.get(0).getNumber());
    }

    @Test
    public void shouldNotTakeUnprivilegedPort() {
        List<Protos.Port> ports = privilegedPort.apply(new HashSet<>(Collections.singletonList(1025)));
        assertTrue(ports.isEmpty());
    }

    @Test
    public void shouldNotTakeMoreThanRequired() {
        Integer port = 0;
        List<Protos.Port> ports = privilegedPort.apply(new HashSet<>(IntStream.range(0, 999).boxed().collect(Collectors.toList())));
        assertEquals(name, ports.get(0).getName());
        assertEquals(port.intValue(), ports.get(0).getNumber());
        assertEquals(1, ports.size());
    }
}