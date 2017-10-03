package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.config.validation.MesosSchedulerPropertiesValidator;
import com.containersolutions.mesos.scheduler.*;
import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.*;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import com.containersolutions.mesos.scheduler.state.StateRepositoryFile;
import com.containersolutions.mesos.scheduler.state.StateRepositoryZookeeper;
import org.apache.mesos.Protos;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationEventPublisher;
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

    @Bean
    public Clock systemClock() {
        return Clock.systemUTC();
    }

    @Bean
    public MesosSchedulerPropertiesValidator configurationPropertiesValidator() {
        return new MesosSchedulerPropertiesValidator();
    }

    @Bean
    @ConditionalOnMissingBean
    public UniversalScheduler scheduler(MesosConfigProperties mesosConfig, OfferStrategyFilter offerStrategyFilter, ApplicationEventPublisher applicationEventPublisher, Supplier<UUID> uuidSupplier, StateRepository stateRepository, TaskMaterializer taskMaterializer, FrameworkInfoFactory frameworkInfoFactory, CredentialFactory credentialFactory) {
        return new UniversalScheduler(mesosConfig, offerStrategyFilter, applicationEventPublisher, uuidSupplier, stateRepository, taskMaterializer, frameworkInfoFactory, credentialFactory);
    }

    @Bean
    public OfferStrategyFilter offerStrategyFilter(Map<String, ResourceRequirement> resourceRequirements) {
        return new OfferStrategyFilter(resourceRequirements);
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
                                .setRole(offer.getResourcesList().stream().filter(resource -> resource.getName().equalsIgnoreCase(name)).findFirst().map(Protos.Resource::getRole).orElse("*"))
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
    public StateRepositoryZookeeper stateRepositoryZookeeper(Environment environment) {
        return new StateRepositoryZookeeper(environment);
    }

    @Bean
    public Supplier<UUID> uuidSupplier() {
        return UUID::randomUUID;
    }

    @Bean
    public MesosConfigProperties mesosConfig() {
        return new MesosConfigProperties();
    }

    @Bean
    @ConditionalOnMissingBean(name = "commandInfoMesosProtoFactory")
    public MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory(MesosConfigProperties mesosConfig) {
        return new CommandInfoMesosProtoFactory(mesosConfig);
    }

    @Bean
    @ConditionalOnMissingBean(TaskInfoFactory.class)
    @ConditionalOnProperty(prefix = "mesos.docker", name = {"image"})
    public TaskInfoFactory taskInfoFactoryDocker(MesosConfigProperties mesosConfig, MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory) {
        return new TaskInfoFactoryDocker(mesosConfig, commandInfoMesosProtoFactory);
    }

    @Bean
    @ConditionalOnMissingBean(TaskInfoFactory.class)
    public TaskInfoFactory taskInfoFactoryCommand(MesosProtoFactory<Protos.CommandInfo, Map<String, String>> commandInfoMesosProtoFactory, Supplier<UUID> uuidSupplier) {
        return new TaskInfoFactoryCommand(commandInfoMesosProtoFactory, uuidSupplier);
    }

    @Bean
    @ConditionalOnMissingBean(name = "distinctHostRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "distinctSlave", havingValue = "true")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ResourceRequirement distinctHostRequirement(Clock systemClock, StateRepository stateRepository) {
        return new DistinctSlaveRequirement(systemClock, stateRepository);
    }

    @Bean
    @ConditionalOnMissingBean(name = "instancesCountRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "count")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ResourceRequirement instancesCountRequirement(StateRepository stateRepository, InstanceCount instanceCount) {
        return new InstancesCountRequirement(stateRepository, instanceCount);
    }

    @Bean
    @ConditionalOnProperty(prefix = "mesos.resources", name = "count")
    public TaskReaper taskReaper(StateRepository stateRepository, InstanceCount instanceCount, UniversalScheduler universalScheduler) {
        return new TaskReaper(stateRepository, instanceCount, universalScheduler);
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
    public ResourceRequirement roleRequirement(MesosConfigProperties mesosConfig) {
        return new RoleRequirement(mesosConfig);
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
    public ResourceRequirement portsRequirement(MesosConfigProperties mesosConfig) {
        return new PortsRequirement(mesosConfig);
    }

    @Bean
    @ConditionalOnMissingBean(name = "volumesRequirements")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement volumesRequirement(MesosConfigProperties mesosConfig) {
        return new VolumesRequirement(mesosConfig);
    }

    @Bean
    public TaskMaterializer taskMaterializer(TaskInfoFactory taskInfoFactory) {
        return new TaskMaterializerMinimal(taskInfoFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public FrameworkInfoFactory frameworkInfoFactory(MesosConfigProperties mesosConfig, StateRepository stateRepository, CredentialFactory credentialFactory) {
        return new FrameworkInfoFactory(mesosConfig, stateRepository, credentialFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public CredentialFactory credentialFactory(MesosConfigProperties mesosConfig) {
        return new CredentialFactory(mesosConfig);
    }

}
