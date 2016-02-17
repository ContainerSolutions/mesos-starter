package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.events.*;
import dk.mwl.mesos.utils.StreamHelper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

public class UniversalScheduler implements Scheduler, ApplicationListener<EmbeddedServletContainerInitializedEvent> {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    OfferStrategyFilter offerStrategyFilter;

    @Value("${mesos.master}")
    protected String mesosMaster;

    @Value("${mesos.role:*}")
    protected String mesosRole;

    @Value("${spring.application.name}")
    protected String applicationName;

    @Autowired
    TaskInfoFactory taskInfoFactory;

    @Autowired
    ApplicationEventPublisher applicationEventPublisher;

    protected AtomicReference<Protos.FrameworkID> frameworkID = new AtomicReference<>(Protos.FrameworkID.newBuilder().setValue("").build());

    protected AtomicReference<SchedulerDriver> driver = new AtomicReference<>();

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        start();
    }

    public void start() {
        Protos.FrameworkInfo.Builder frameworkBuilder = Protos.FrameworkInfo.newBuilder()
                .setName(applicationName)
                .setUser("root")
                .setRole(mesosRole)
                .setCheckpoint(true)
                .setFailoverTimeout(10.0)
                .setId(frameworkID.get());

        logger.info("Starting Framework");

        MesosSchedulerDriver driver = new MesosSchedulerDriver(this, frameworkBuilder.build(), mesosMaster);
        if (!this.driver.compareAndSet(null, driver)) {
            throw new IllegalStateException("Driver already initialised");
        }
        driver.start();
    }

    @PreDestroy
    public void stop() throws ExecutionException, InterruptedException {
        driver.get().stop(false);
        logger.info("Scheduler stopped");
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
        offers.stream()
                .peek(offer -> logger.info("Received offerId=" + offer.getId().getValue() + " for slaveId=" + offer.getSlaveId().getValue()))
                .map(offer -> offerStrategyFilter.evaluate(offer))
                .filter(StreamHelper.onNegative(
                        OfferEvaluation::isValid,
                        offerEvaluation -> {
                            logger.info("Declining offerId=" + offerEvaluation.getOffer().getId().getValue());
                            schedulerDriver.declineOffer(offerEvaluation.offer.getId());
                        }))
                .peek(offerEvaluation -> {
                    logger.info("Accepting offer offerId=" + offerEvaluation.getOffer().getId().getValue() + " on slaveId=" + offerEvaluation.getOffer().getSlaveId().getValue());
                })
                .map(offerEvaluation -> new TaskProposal(offerEvaluation.offer, taskInfoFactory.create(offerEvaluation.offer, offerEvaluation.resources)))
                .forEach(taskProposal -> schedulerDriver.launchTasks(Collections.singleton(taskProposal.getOfferId()), Collections.singleton(taskProposal.getTaskInfo())));
    }

    private static class TaskProposal {
        Protos.Offer offer;
        Protos.TaskInfo taskInfo;

        public TaskProposal(Protos.Offer offer, Protos.TaskInfo taskInfo) {
            this.offer = offer;
            this.taskInfo = taskInfo;
        }

        public Protos.OfferID getOfferId() {
            return offer.getId();
        }

        public Protos.TaskInfo getTaskInfo() {
            return taskInfo;
        }
    }

    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {
        logger.info("Offer rescinded offerId=" + offerID.getValue());
    }

    @Override
    public void statusUpdate(SchedulerDriver schedulerDriver, Protos.TaskStatus taskStatus) {
        logger.debug("Received status update: " + taskStatus.getMessage());
        applicationEventPublisher.publishEvent(new StatusUpdateEvent(taskStatus));
    }

    @Override
    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] data) {
        logger.debug("Received framework message from slaveId=" + slaveID.getValue());
        applicationEventPublisher.publishEvent(new FrameworkMessageEvent(data, executorID, slaveID));
    }

    @Override
    public void disconnected(SchedulerDriver schedulerDriver) {
        logger.debug("Disconnected");
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
        logger.info("Received error: " + message);
        applicationEventPublisher.publishEvent(new ErrorEvent(message));
    }
}
