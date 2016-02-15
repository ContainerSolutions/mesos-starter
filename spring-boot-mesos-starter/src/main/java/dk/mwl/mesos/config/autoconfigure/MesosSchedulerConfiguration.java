package dk.mwl.mesos.config.autoconfigure;

import dk.mwl.mesos.scheduler.OfferStrategyFilter;
import dk.mwl.mesos.scheduler.UniversalScheduler;
import org.apache.mesos.Scheduler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MesosSchedulerConfiguration {

    @Bean
    public Scheduler scheduler() {
        return new UniversalScheduler();
    }

    @Bean
    public OfferStrategyFilter offerStrategyFilter() {
        return new OfferStrategyFilter();
    }
}
