package com.containersolutions.mesos.config.autoconfigure;

import com.containersolutions.mesos.scheduler.*;
import com.containersolutions.mesos.scheduler.config.DockerConfigProperties;
import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.*;
import com.containersolutions.mesos.scheduler.requirements.ports.PortParser;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import com.containersolutions.mesos.scheduler.state.StateRepositoryFile;
import com.containersolutions.mesos.scheduler.state.StateRepositoryZookeeper;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.state.State;
import org.apache.mesos.state.ZooKeeperState;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.function.Supplier;

@Configuration
public class MesosSchedulerConfiguration {

    @Autowired
    Environment environment;

    @Bean
    public Scheduler scheduler() {
        return new UniversalScheduler();
    }

    @Bean
    public OfferStrategyFilter offerStrategyFilter() {
        return new OfferStrategyFilter();
    }

    private ResourceRequirement simpleScalarRequirement(String name, double minimumRequirement) {
        return (requirement, taskId, offer) -> new OfferEvaluation(
                requirement,
                taskId,
                offer,
                ResourceRequirement.scalarSum(offer, name) > minimumRequirement,
                Protos.Resource.newBuilder()
                        .setType(Protos.Value.Type.SCALAR)
                        .setName(name)
                        .setScalar(Protos.Value.Scalar.newBuilder().setValue(minimumRequirement))
                        .build()
        );

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

    @Bean
    @ConditionalOnMissingBean(StateRepository.class)
    public StateRepository stateRepositoryZookeeper() {
        return new StateRepositoryZookeeper();
    }

    @Bean
    @ConditionalOnProperty(prefix = "mesos.zookeeper", name = "server")
    public State zkState() {
        return new ZooKeeperState(
                environment.getRequiredProperty("mesos.zookeeper.server"),
                1000,
                TimeUnit.MILLISECONDS,
                "/" + environment.getProperty("mesos.framework.name", "default")
        );
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
    public MesosProtoFactory<Protos.CommandInfo.Builder> commandInfoMesosProtoFactory() {
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
    @ConditionalOnProperty(prefix = "mesos", name = {"command"})
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
    @ConditionalOnMissingBean(name = "scaleFactorRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "scale")
    @Order(Ordered.LOWEST_PRECEDENCE)
    public ResourceRequirement scaleFactorRequirement() {
        return new ScaleFactorRequirement(environment.getProperty("mesos.resources.scale", Integer.class, 1));
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
    public ResourceRequirement cpuRequirement() {
        return simpleScalarRequirement("cpus", environment.getRequiredProperty("mesos.resources.cpus", Double.class));
    }

    @Bean
    @ConditionalOnMissingBean(name = "memRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "mem")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement memRequirement() {
        return simpleScalarRequirement("mem", environment.getRequiredProperty("mesos.resources.mem", Double.class));
    }

    @Bean
    @ConditionalOnMissingBean(name = "portsRequirement")
    @ConditionalOnProperty(prefix = "mesos.resources", name = "ports")
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ResourceRequirement portsRequirement(MesosConfigProperties mesosConfig, PortParser portParser) {
        return new PortRequirement(mesosConfig, portParser);
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

    @Bean
    @ConditionalOnMissingBean
    public PortParser portParser(MesosConfigProperties mesosConfig) {
        return new PortParser(mesosConfig);
    }

    @Bean
    @ConditionalOnMissingBean
    public DockerConfigProperties dockerConfigProperties() {
        return new DockerConfigProperties();
    }
}
