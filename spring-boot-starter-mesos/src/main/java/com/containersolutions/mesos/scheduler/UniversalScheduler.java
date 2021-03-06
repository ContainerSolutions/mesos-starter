package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.events.*;
import com.containersolutions.mesos.scheduler.requirements.OfferEvaluation;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import com.containersolutions.mesos.utils.StreamHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.EventListener;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class UniversalScheduler implements Scheduler, ApplicationListener<ApplicationReadyEvent> {
    protected final Log logger = LogFactory.getLog(getClass());

    @Value("${mesos.master}")
    protected String mesosMaster;

    @Value("${mesos.zookeeper.server}")
    protected String zookeeperMaster;

    @Value("${spring.application.name}")
    protected String applicationName;

    private final MesosConfigProperties mesosConfig;

    private final OfferStrategyFilter offerStrategyFilter;

    private final ApplicationEventPublisher applicationEventPublisher;

    private final Supplier<UUID> uuidSupplier;

    private final StateRepository stateRepository;

    private final TaskMaterializer taskMaterializer;

    private final FrameworkInfoFactory frameworkInfoFactory;

    private final CredentialFactory credentialFactory;

    protected AtomicReference<Protos.FrameworkID> frameworkID = new AtomicReference<>();

    protected AtomicReference<SchedulerDriver> driver = new AtomicReference<>();

    public UniversalScheduler(MesosConfigProperties mesosConfig, OfferStrategyFilter offerStrategyFilter, ApplicationEventPublisher applicationEventPublisher, Supplier<UUID> uuidSupplier, StateRepository stateRepository, TaskMaterializer taskMaterializer, FrameworkInfoFactory frameworkInfoFactory, CredentialFactory credentialFactory) {
        this.mesosConfig = mesosConfig;
        this.offerStrategyFilter = offerStrategyFilter;
        this.applicationEventPublisher = applicationEventPublisher;
        this.uuidSupplier = uuidSupplier;
        this.stateRepository = stateRepository;
        this.taskMaterializer = taskMaterializer;
        this.frameworkInfoFactory = frameworkInfoFactory;
        this.credentialFactory = credentialFactory;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        start();
    }

    public void start() {
        logger.info("Starting Mesos-Starter Framework");

        MesosSchedulerDriver driver;
        Protos.Credential credential = credentialFactory.create();
        final String zookeeperUrl = "zk://" + zookeeperMaster + "/mesos";
        if (credential.isInitialized()) {
            logger.debug("Starting scheduler driver with supplied credentials for principal=" + credential.getPrincipal());
            driver = new MesosSchedulerDriver(this, frameworkInfoFactory.create().build(), zookeeperUrl, credential);
        } else {
            logger.debug("Starting scheduler driver without authorisation.");
            //TODO: The follow MesosSchedulerDriver constructor will be deprecated at some point.
            driver = new MesosSchedulerDriver(this, frameworkInfoFactory.create().build(), zookeeperUrl);
        }

        if (!this.driver.compareAndSet(null, driver)) {
            throw new IllegalStateException("Driver already initialised");
        }
        driver.start();

        new Thread(driver::join).start();
    }

    @PreDestroy
    public void stop() {
        if (driver.get() != null) {
            driver.get().abort();
            logger.info("Driver aborted");
        }
    }


    @Override
    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        logger.info("Framework registrered with frameworkId=" + frameworkID.getValue());
        this.frameworkID.set(frameworkID);
        applicationEventPublisher.publishEvent(new FrameworkRegistreredEvent(frameworkID, masterInfo));
    }

    @Override
    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {
        logger.info("Framework re-registrered");
        applicationEventPublisher.publishEvent(new FrameworkReregistreredEvent(masterInfo));
    }

    @Override
    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {
        logger.info("Initiating new offer round of " + offers.size() + " offers");
        AtomicInteger acceptedOffers = new AtomicInteger(0);
        AtomicInteger rejectedOffers = new AtomicInteger(0);
        offers.stream()
                .peek(offer -> logger.debug("Received offerId=" + offer.getId().getValue() + " for slaveId=" + offer.getSlaveId().getValue()))
                .map(offer -> offerStrategyFilter.evaluate(uuidSupplier.get().toString(), offer))
                .peek(offerEvaluation -> (offerEvaluation.isValid() ? acceptedOffers : rejectedOffers).incrementAndGet())
                .filter(StreamHelper.onNegative(
                        OfferEvaluation::isValid,
                        offerEvaluation -> schedulerDriver.declineOffer(offerEvaluation.getOffer().getId())))
                .peek(offerEvaluation -> logger.info("Accepting offer offerId=" + offerEvaluation.getOffer().getId().getValue() + " on slaveId=" + offerEvaluation.getOffer().getSlaveId().getValue()))
                .map(taskMaterializer::createProposal)
//                .peek(taskProposal -> logger.debug("Launching task " + taskProposal.getTaskInfo().toString()))
                .forEach(taskProposal -> {
                    schedulerDriver.launchTasks(Collections.singleton(taskProposal.getOfferId()), Collections.singleton(taskProposal.getTaskInfo()));
                    stateRepository.store(taskProposal.taskInfo);
                });
        logger.info("Finished evaluating " + offers.size() + " offers. Accepted " + acceptedOffers.get() + " offers and rejected " + rejectedOffers.get());
    }

    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        logger.warn("Offer rescinded offerId=" + offerID.getValue());
    }

    @Override
    public void statusUpdate(SchedulerDriver schedulerDriver, Protos.TaskStatus taskStatus) {
        if (taskStatus.getState().equals(Protos.TaskState.TASK_ERROR)) {
            logger.warn("Received status update for taskID=" + taskStatus.getTaskId().getValue() + " state=" + taskStatus.getState() + " message='" + taskStatus.getMessage() + "' ");
        }

        applicationEventPublisher.publishEvent(new StatusUpdateEvent(taskStatus));
    }

    @Override
    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] data) {
        logger.debug("Received framework message from slaveId=" + slaveID.getValue());
        applicationEventPublisher.publishEvent(new FrameworkMessageEvent(data, executorID, slaveID));
    }

    @Override
    public void disconnected(SchedulerDriver schedulerDriver) {
        logger.warn("Disconnected");
    }

    @Override
    public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {
        logger.info("Received slave lost on slaveId=" + slaveID.getValue());
        applicationEventPublisher.publishEvent(new SlaveLostEvent(slaveID));
    }

    @Override
    public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, int status) {
        logger.info("Received executor lost on slaveId=" + slaveID.getValue());
        applicationEventPublisher.publishEvent(new ExecutorLostEvent(status, executorID, slaveID));
    }

    @Override
    public void error(SchedulerDriver schedulerDriver, String message) {
        logger.error("Received error: " + message);

        if (message.equalsIgnoreCase("Framework has been removed")) {
            applicationEventPublisher.publishEvent(new FrameworkRemovedEvent(message));
        }
        applicationEventPublisher.publishEvent(new ErrorEvent(message));
    }

    public void killTask(Protos.TaskID taskId) {
        driver.get().killTask(taskId);
    }

    public void sendFrameworkMessage(String executorId, String slaveId, byte[] data) {
        driver.get().sendFrameworkMessage(Protos.ExecutorID.newBuilder().setValue(executorId).build(), Protos.SlaveID.newBuilder().setValue(slaveId).build(), data);
    }

    @EventListener
    public void onTearDownFrameworkEvent(TearDownFrameworkEvent event) {
        driver.get().stop();
        logger.info("Driver stopped");
        driver.set(null);
    }

}
