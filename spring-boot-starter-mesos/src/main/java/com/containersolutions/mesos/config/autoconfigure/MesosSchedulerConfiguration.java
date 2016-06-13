package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.config.validation.MesosSchedulerPropertiesValidator;
import com.containersolutions.mesos.scheduler.*;
import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.*;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import com.containersolutions.mesos.scheduler.state.StateRepositoryFile;
import com.containersolutions.mesos.scheduler.state.StateRepositoryZookeeper;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

@Configuration
public class MesosSchedulerConfiguration {

    @Autowired
    Environment environment;

    @Bean
    public MesosSchedulerPropertiesValidator configurationPropertiesValidator() {
        return new MesosSchedulerPropertiesValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public Scheduler scheduler() {
        return new UniversalScheduler();
    }

    @Bean
    public OfferStrategyFilter offerStrategyFilter() {
        return new OfferStrategyFilter();
    }

    private ResourceRequirement simpleScalarRequirement(String name, double minimumRequirement) {
        return (requirement, taskId, offer) -> {
            if (ResourceRequirement.scalarSum(offer, name) > minimumRequirement) {
                return OfferEvaluation.accept(
                        requirement,
                        taskId,
                        offer,
                        Collections.emptyMap(),
                        Collections.emptyList(),
                        Collections.emptyList(),
                        Protos.Resource.newBuilder()
                                .setType(Protos.Value.Type.SCALAR)
                                .setName(name)
                                .setScalar(Protos.Value.Scalar.newBuilder().setValue(minimumRequirement))
                                .build()
                );
            }
            return OfferEvaluation.decline(requirement, taskId, offer, "Not enough resources for " + name);
        };

    }

    @Bean
    public AtomicMarkableReference<Protos.FrameworkID> frameworkId() {
        return new AtomicMarkableReference<>(Protos.FrameworkID.newBuilder().setValue("").build(), false);
    }

    @Bean
    @ConditionalOnProperty(prefix = "mesos.state.file", name = "location")
    public StateRepository stateRepositoryFile() {
        return new StateRepositoryFile();
    }

    @Bean(initMethod = "connect")
    @ConditionalOnMissingBean(StateRepository.class)
    public StateRepositoryZookeeper stateRepositoryZookeeper() {
        return new StateRepositoryZookeeper();
    }

    @Bean
    public Supplier<UUID> uuidSupplier() {
        return UUID::randomUUID;
    }

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }

    @Bean
    public MesosConfigProperties mesosConfig() {
        return new MesosConfigProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "commandInfoMesosProtoFactory")
    public MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory() {
        return new CommandInfoMesosProtoFactory();
    }

    @Bean
    @ConditionalOnMissingBean(TaskInfoFactory.class)
    @ConditionalOnProperty(prefix = "mesos.docker", name = {"image"})
    public TaskInfoFactory taskInfoFactoryDocker() {
        return new TaskInfoFactoryDocker();
    }

    @Bean
    @ConditionalOnMissingBean(TaskInfoFactory.class)
    public TaskInfoFactory taskInfoFactoryCommand() {
        return new TaskInfoFactoryCommand();
    }

    @Bean
    @ConditionalOnMissingBean(name = "distinctHostRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "distinctSlave", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ResourceRequirement distinctHostRequirement() {
        return new DistinctSlaveRequirement();
    }

    @Bean
    @ConditionalOnMissingBean(name = "instancesCountRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "count")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ResourceRequirement instancesCountRequirement() {
        return new InstancesCountRequirement();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mesos.resources", name = "count")
    public TaskReaper taskReaper() {
        return new TaskReaper();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mesos.resources", name = "count")
    public InstanceCount instanceCount(Environment env) {
        return new InstanceCount(env.getProperty("mesos.resources.count", Integer.class, 1));
    }

    @Bean
    @ConditionalOnMissingBean(name = "roleRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "role", havingValue = "all")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ResourceRequirement roleRequirement() {
        return new RoleRequirement();
    }


    @Bean
    @ConditionalOnMissingBean(name = "cpuRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "cpus")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement cpuRequirement(MesosConfigProperties mesosConfigProperties) {
        return simpleScalarRequirement("cpus", mesosConfigProperties.getResources().getCpus());
    }

    @Bean
    @ConditionalOnMissingBean(name = "memRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "mem")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement memRequirement(Environment environment) {
        return simpleScalarRequirement("mem", environment.getRequiredProperty("mesos.resources.mem", Double.class));
    }

    @Bean
    @ConditionalOnMissingBean(name = "portsRequirement")
//    @ConditionalOnProperty(prefix = "mesos.resources", name = "ports")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement portsRequirement() {
        return new PortsRequirement();
    }

    @Bean
    @ConditionalOnMissingBean(name = "volumesRequirements")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement volumesRequirement() {
        return new VolumesRequirement();
    }

    @Bean
    public TaskMaterializer taskMaterializer() {
        return new TaskMaterializerMinimal();
    }

    @Bean
    @ConditionalOnMissingBean
    public FrameworkInfoFactory frameworkInfoFactory() {
        return new FrameworkInfoFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public CredentialFactory credentialFactory() {
        return new CredentialFactory();
    }

}
