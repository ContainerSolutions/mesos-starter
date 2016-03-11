package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(HealthIndicator.class)
@AutoConfigureBefore({org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration.class})
@AutoConfigureAfter({MesosSchedulerConfiguration.class})
public class TaskActuatorConfiguration {
    @Autowired
    StateRepository stateRepository;

    @Value("${mesos.resources.scale:1}")
    Integer scale;

    @Bean
    public HealthIndicator taskHealthIndicator() {
        return new AbstractHealthIndicator() {
            @Override
            protected void doHealthCheck(Health.Builder builder) throws Exception {
                if (stateRepository.allTaskInfos().size() == scale) {
                    builder.up();
                } else {
                    builder.down();
                }
                builder.withDetail("scale", "" + stateRepository.allTaskInfos().size() + " out of " + scale + " tasks are operational");
            }
        };
    }
}

