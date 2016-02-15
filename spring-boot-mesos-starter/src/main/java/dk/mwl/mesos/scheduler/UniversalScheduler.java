package dk.mwl.mesos.scheduler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.MesosSchedulerDriver;
import org.apache.mesos.Protos;
import org.apache.mesos.Scheduler;
import org.apache.mesos.SchedulerDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerInitializedEvent;
import org.springframework.context.ApplicationListener;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class UniversalScheduler implements Scheduler, ApplicationListener<EmbeddedServletContainerInitializedEvent> {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    OfferStrategyFilter offerStrategyFilter;

    @Value("mesos.master")
    protected String mesosMaster;

    protected AtomicReference<Protos.FrameworkID> frameworkID;

    @Override
    public void onApplicationEvent(EmbeddedServletContainerInitializedEvent event) {
        start();
    }

    public void start() {
        if (frameworkID.get() == null) {
            throw new IllegalStateException("Starting framework before it has been registrered");
        }

        Protos.FrameworkInfo.Builder frameworkBuilder = Protos.FrameworkInfo.newBuilder()
                .setName("demo")
                .setUser("demo")
                .setRole("*")
                .setCheckpoint(true)
                .setFailoverTimeout(10.0)
                .setId(frameworkID.get());

        logger.info("Starting Framework");

        MesosSchedulerDriver driver = new MesosSchedulerDriver(this, frameworkBuilder.build(), mesosMaster);
        driver.start();
    }


    @Override
    public void registered(SchedulerDriver schedulerDriver, Protos.FrameworkID frameworkID, Protos.MasterInfo masterInfo) {
        if (!this.frameworkID.compareAndSet(null, frameworkID)) {
            throw new IllegalStateException("Frame is already registrered");
        }
    }

    @Override
    public void reregistered(SchedulerDriver schedulerDriver, Protos.MasterInfo masterInfo) {

    }

    @Override
    public void resourceOffers(SchedulerDriver schedulerDriver, List<Protos.Offer> offers) {
        offerStrategyFilter.accept(offers);

//        schedulerDriver.acceptOffers()
    }

    @Override
    public void offerRescinded(SchedulerDriver schedulerDriver, Protos.OfferID offerID) {

    }

    @Override
    public void statusUpdate(SchedulerDriver schedulerDriver, Protos.TaskStatus taskStatus) {

    }

    @Override
    public void frameworkMessage(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, byte[] data) {

    }

    @Override
    public void disconnected(SchedulerDriver schedulerDriver) {

    }

    @Override
    public void slaveLost(SchedulerDriver schedulerDriver, Protos.SlaveID slaveID) {

    }

    @Override
    public void executorLost(SchedulerDriver schedulerDriver, Protos.ExecutorID executorID, Protos.SlaveID slaveID, int status) {

    }

    @Override
    public void error(SchedulerDriver schedulerDriver, String message) {

    }
}
