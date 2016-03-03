package com.containersolutions.mesos.config.autoconfigure;

import org.apache.mesos.state.State;
import org.springframework.boot.actuate.autoconfigure.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Configuration
@ConditionalOnClass(HealthIndicator.class)
@AutoConfigureBefore({org.springframework.boot.actuate.autoconfigure.EndpointAutoConfiguration.class})
@AutoConfigureAfter({MesosSchedulerConfiguration.class})
public class MesosSchedulerActuatorConfiguration {
    @Bean
    @ConditionalOnEnabledHealthIndicator("zookeeper")
    @ConditionalOnProperty(prefix = "mesos.zookeeper", name = "server")
    public HealthIndicator zookeeperHealthIndicator(State state) {
        return new AbstractHealthIndicator() {
            @Override
            protected void doHealthCheck(Health.Builder builder) throws Exception {
                CountDownLatch latch = new CountDownLatch(1);

                final Thread thread = new Thread(() -> {
                    try {
                        state.names().get(1, TimeUnit.SECONDS).hasNext();
                    } catch (Exception e) {
                        builder.down(e);
                    }
                    latch.countDown();
                });
                thread.setDaemon(true);
                thread.start();
                if (latch.await(1, TimeUnit.SECONDS)) {
                    builder.up();
                }
                else {
                    builder.down();
                }
            }
        };
    }

}
