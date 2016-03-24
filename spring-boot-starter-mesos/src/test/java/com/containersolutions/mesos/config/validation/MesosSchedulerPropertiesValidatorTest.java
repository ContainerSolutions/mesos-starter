package com.containersolutions.mesos.config.validation;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import static org.junit.Assert.assertNotNull;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = MesosSchedulerPropertiesValidatorTest.SimpleConfiguration.class)
@TestPropertySource(properties = {"mesos.master: none"})
public class MesosSchedulerPropertiesValidatorTest {
    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Autowired
    MesosSchedulerPropertiesValidator validator;

    @Configuration
    public static class SimpleConfiguration {
        @Bean
        public MesosSchedulerPropertiesValidator configurationPropertiesValidator() {
            return new MesosSchedulerPropertiesValidator();
        }

        @Bean
        public MesosConfigProperties mesosConfigProperties() {
            return new MesosConfigProperties();
        }
    }

    @Test
    public void name() throws Exception {
        assertNotNull(validator);
    }
}