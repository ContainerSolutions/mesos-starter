package dk.mwl.mesos.scheduler.requirements;

import dk.mwl.mesos.scheduler.events.StatusUpdateEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.time.Clock;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Arrays.asList;

public class DistinctSlaveRequirement implements ResourceRequirement, ApplicationListener<StatusUpdateEvent> {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    Clock clock;

    Set<String> slaveIds = new HashSet<>();
    Map<String, Instant> tentativeAccept = new ConcurrentHashMap<>();


    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        final Instant now = clock.instant();
        cleanUpTentatives(now);

        final String slaveId = offer.getSlaveId().getValue();
        final boolean valid = slaveIsRunningTask(slaveId);
        if (valid) {
            tentativeAccept.put(slaveId, now.plusSeconds(60));
        }
        return new OfferEvaluation(requirement, taskId, offer, valid);
    }

    private boolean slaveIsRunningTask(String slaveId) {
        return !slaveIds.contains(slaveId) && !tentativeAccept.containsKey(slaveId);
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

    private void cleanUpTentatives(Instant now) {
        tentativeAccept.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(now))
                .map(Map.Entry::getKey)
                .peek(key -> logger.debug("removing key = " + key))
                .forEach(tentativeAccept::remove);
    }

}
