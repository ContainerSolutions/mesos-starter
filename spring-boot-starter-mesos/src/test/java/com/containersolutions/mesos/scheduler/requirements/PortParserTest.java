package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.ports.PortFunction;
import com.containersolutions.mesos.scheduler.requirements.ports.PortParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties.PortType.*;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class PortParserTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    MesosConfigProperties mesosConfigProperties;

    @InjectMocks
    PortParser portParser;

    @Test
    public void shouldCreateFunctionsForFixedPorts() {
        Map<String, String> portMap = new HashMap<>();
        String key1 = "PORT_123";
        String key2 = "PORT_1234";
        int value1 = 89065;
        int value2 = 452054;
        portMap.put(key1, Integer.toString(value1));
        portMap.put(key2, Integer.toString(value2));
        when(mesosConfigProperties.getResources().getPorts()).thenReturn(portMap);
        List<PortFunction> ports = portParser.getPorts();
        assertEquals(portMap.size(), ports.size());
    }

    @Test
    public void shouldReturnCorrectSize() {
        Map<String, String> map = enumMap();
        map.put("Fixed", "1234");
        when(mesosConfigProperties.getResources().getPorts()).thenReturn(map);
        assertEquals(map.size(), portParser.size().intValue());
    }

    private Map<String, String> enumMap() {
        Map<String, String> portMap = new HashMap<>();
        portMap.put("KEY_" + ANY.toString(), ANY.toString());
        portMap.put("KEY_" + UNPRIVILEDGED.toString(), UNPRIVILEDGED.toString());
        portMap.put("KEY_" + PRIVILEDGED.toString(), PRIVILEDGED.toString());
        return portMap;
    }
}