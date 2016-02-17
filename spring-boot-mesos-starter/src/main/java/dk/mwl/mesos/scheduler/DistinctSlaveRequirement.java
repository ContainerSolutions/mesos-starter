package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.mesos.Protos;
import org.springframework.context.ApplicationListener;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;

public class DistinctSlaveRequirement implements ResourceRequirement, ApplicationListener<StatusUpdateEvent> {
    Set<String> slaveIds = new HashSet<>();

    @Override
    public OfferEvaluation apply(Protos.Offer offer) {
        return new OfferEvaluation(offer, !slaveIds.contains(offer.getSlaveId().getValue()));
    }

    @Override
    public void onApplicationEvent(StatusUpdateEvent event) {
        final Protos.TaskState state = event.getTaskStatus().getState();
        final String slaveId = event.getTaskStatus().getSlaveId().getValue();
        if (state == Protos.TaskState.TASK_RUNNING) {
            slaveIds.add(slaveId);
        }
        else if (asList(Protos.TaskState.TASK_FINISHED, Protos.TaskState.TASK_FAILED, Protos.TaskState.TASK_KILLED, Protos.TaskState.TASK_LOST, Protos.TaskState.TASK_ERROR).contains(state)) {
            slaveIds.remove(slaveId);
        }
    }
}
