package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(MockitoJUnitRunner.class)
public class FixedPortTest {

    private String name = "test";
    private Integer port = 7895;
    private PortFactory.FixedPort fixedPort = new PortFactory.FixedPort(name, port);

    @Test
    public void shouldCreateFixedPort() {
        List<Protos.Port> ports = fixedPort.apply(new HashSet<>(Collections.singletonList(port)));
        assertEquals(name, ports.get(0).getName());
        assertEquals(port.intValue(), ports.get(0).getNumber());
    }

    @Test
    public void shouldNotReturnPortWhenNotAvailable() {
        List<Protos.Port> ports = fixedPort.apply(new HashSet<>(Collections.singletonList(89)));
        assertTrue(ports.isEmpty());
    }
}