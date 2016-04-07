package com.containersolutions.mesos.config.validation;

import com.containersolutions.mesos.scheduler.config.*;
import org.junit.Test;
import org.springframework.validation.DirectFieldBindingResult;
import org.springframework.validation.Errors;

import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class MesosSchedulerPropertiesValidatorTest {
    private MesosConfigProperties config = new MesosConfigProperties();

    private Errors errors = new DirectFieldBindingResult(config, "mesos");


    private MesosSchedulerPropertiesValidator validator = new MesosSchedulerPropertiesValidator();

    @Test
    public void willRejectIfMasterIsEmpty() throws Exception {
        config.setMaster(" ");

        validator.validate(config, errors);
        assertEquals("master.empty", errors.getFieldError("master").getCode());
    }

    @Test
    public void willRejectCountIfNegative() throws Exception {
        config.setResources(new ResourcesConfigProperties());
        config.getResources().setCount(-1);

        validator.validate(config, errors);
        assertEquals("resources.count.not_positive", errors.getFieldError("resources.count").getCode());


    }
    @Test
    public void willRejectCpuIfNegative() throws Exception {
        config.setResources(new ResourcesConfigProperties());
        config.getResources().setCpus(-1.0);

        validator.validate(config, errors);
        assertEquals("resources.cpus.not_positive", errors.getFieldError("resources.cpus").getCode());

    }

    @Test
    public void willRejectMemIfNegative() throws Exception {
        config.setResources(new ResourcesConfigProperties());
        config.getResources().setMem(-128.0);

        validator.validate(config, errors);
        assertEquals("resources.mem.not_positive", errors.getFieldError("resources.mem").getCode());
    }

    @Test
    public void willRequireHostPort() throws Exception {
        config.setResources(new ResourcesConfigProperties() {{
            setPorts(Collections.singletonMap("test", new ResourcePortConfigProperties() {{
                setContainer(1);
            }}));
        }});

        validator.validate(config, errors);

        assertEquals("resources.ports.test.host.empty", errors.getFieldError("resources.ports").getCode());
    }

    @Test
    public void willRequireContainerPortWhenContainerized() throws Exception {
        config.setDocker(new DockerConfigProperties() {{
            setImage("test");
        }});
        config.setResources(new ResourcesConfigProperties() {{
            setPorts(Collections.singletonMap("test", new ResourcePortConfigProperties() {{
                setHost("ANY");
            }}));
        }});

        validator.validate(config, errors);

        assertEquals("resources.ports.test.container.empty", errors.getFieldError("resources.ports").getCode());
    }

    @Test
    public void willRejectContainerPortWhenNotContainerized() throws Exception {
        config.setResources(new ResourcesConfigProperties() {{
            setPorts(Collections.singletonMap("test", new ResourcePortConfigProperties() {{
                setHost("ANY");
                setContainer(1);
            }}));
        }});

        validator.validate(config, errors);

        assertEquals("resources.ports.test.container.not_containerized", errors.getFieldError("resources.ports").getCode());
    }

    @Test
    public void willNotRejectAGoodConfiguration() throws Exception {
        config.setMaster("leader.mesos:5050");
        config.setZookeeper(new ZookeeperConfigProperties() {{
            setServer("leader.mesos:2181");
        }});
        config.setResources(new ResourcesConfigProperties() {{
            setCpus(1.0);
            setMem(128);
        }});

        validator.validate(config, errors);

        assertEquals(0, errors.getErrorCount());
    }
}