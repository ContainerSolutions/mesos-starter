package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class UnprivilegedPortTest {

    private String name = "test";
    private UnprivilegedPort unprivilegedPort = new UnprivilegedPort(name);

    @Test
    public void shouldShouldTakeUnprivilegedPort() {
        Long port = 1025L;
        List<Protos.Port> ports = unprivilegedPort.apply(new HashSet<>(Collections.singletonList(port)));
        assertEquals(name, ports.get(0).getName());
        assertEquals(port.intValue(), ports.get(0).getNumber());
    }

    @Test
    public void shouldNotTakePrivilegedPort() {
        List<Protos.Port> ports = unprivilegedPort.apply(new HashSet<>(Collections.singletonList(0L)));
        assertTrue(ports.isEmpty());
    }

    @Test
    public void shouldNotTakeMoreThanRequired() {
        Long port = 1024L;
        List<Protos.Port> ports = unprivilegedPort.apply(new HashSet<>(LongStream.range(102, 9999).boxed().collect(Collectors.toList())));
        assertEquals(name, ports.get(0).getName());
        assertEquals(port.intValue(), ports.get(0).getNumber());
        assertEquals(1, ports.size());
    }
}