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
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Arrays.asList;

public class ScaleFactorRequirement implements ResourceRequirement, ApplicationListener<StatusUpdateEvent> {
    protected final Log logger = LogFactory.getLog(getClass());

    @Autowired
    Clock clock;

    AtomicInteger scaleFactor = new AtomicInteger(1);
    Set<String> runningTasks = new HashSet<>();

    Map<String, Instant> tentativeAccept = new ConcurrentHashMap<>();

    public ScaleFactorRequirement(int scaleFactor) {
        this.scaleFactor.set(scaleFactor);
    }

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        final Instant now = clock.instant();
        cleanUpTentatives(now);
        final boolean valid = totalInstances() < scaleFactor.get();
        if (valid) {
            tentativeAccept.put(taskId, now.plusSeconds(60));
        }
        return new OfferEvaluation(requirement, taskId, offer, valid);
    }

    private int totalInstances() {
        return runningTasks.size() + tentativeAccept.size();
    }

    @Override
    public void onApplicationEvent(StatusUpdateEvent event) {
        final Protos.TaskState state = event.getTaskStatus().getState();
        final String taskId = event.getTaskStatus().getTaskId().getValue();
        tentativeAccept.remove(taskId);

        if (state == Protos.TaskState.TASK_RUNNING) {
            runningTasks.add(taskId);
        }
        else if (asList(Protos.TaskState.TASK_FINISHED, Protos.TaskState.TASK_FAILED, Protos.TaskState.TASK_KILLED, Protos.TaskState.TASK_LOST, Protos.TaskState.TASK_ERROR).contains(state)) {
            if (!runningTasks.remove(taskId)) {
                logger.warn("Notified about unknown task taskId=" + taskId);
            }
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
