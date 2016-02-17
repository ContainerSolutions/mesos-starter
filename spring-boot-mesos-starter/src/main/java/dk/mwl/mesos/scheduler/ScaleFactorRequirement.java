package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.mesos.Protos;
import org.springframework.context.ApplicationListener;

import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

public class ScaleFactorRequirement implements ResourceRequirement, ApplicationListener<StatusUpdateEvent> {
    AtomicInteger scaleFactor = new AtomicInteger(1);
    AtomicInteger instances = new AtomicInteger(0);

    public ScaleFactorRequirement(int scaleFactor) {
        this.scaleFactor.set(scaleFactor);
    }

    @Override
    public OfferEvaluation apply(Protos.Offer offer) {
        return new OfferEvaluation(offer, instances.get() < scaleFactor.get());
    }

    @Override
    public void onApplicationEvent(StatusUpdateEvent event) {
        final Protos.TaskState state = event.getTaskStatus().getState();
        if (state == Protos.TaskState.TASK_RUNNING) {
            instances.incrementAndGet();
        }
        else if (asList(Protos.TaskState.TASK_FINISHED, Protos.TaskState.TASK_FAILED, Protos.TaskState.TASK_KILLED, Protos.TaskState.TASK_LOST, Protos.TaskState.TASK_ERROR).contains(state)) {
            instances.decrementAndGet();
        }
    }
}
