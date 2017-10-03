package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.containersolutions.mesos.utils.MesosHelper.isTerminalTaskState;

public class DistinctSlaveRequirement implements ResourceRequirement {
    protected final Log logger = LogFactory.getLog(getClass());

    private final Clock clock;

    private final StateRepository stateRepository;

    private Map<String, Instant> tentativeAccept = new ConcurrentHashMap<>();

    public DistinctSlaveRequirement(Clock clock, StateRepository stateRepository) {
        this.clock = clock;
        this.stateRepository = stateRepository;
    }

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        final Instant now = clock.instant();
        cleanUpTentatives(now);

        final String slaveId = offer.getSlaveId().getValue();
        final boolean valid = slaveIsRunningTask(slaveId);
        if (valid) {
            tentativeAccept.put(slaveId, now.plusSeconds(60));
            return OfferEvaluation.accept(requirement, taskId, offer, Collections.emptyMap(), Collections.emptyList(), Collections.emptyList());
        }
        return OfferEvaluation.decline(requirement, taskId, offer, "Slave " + slaveId + " is already running task");
    }

    private boolean slaveIsRunningTask(String slaveId) {
        return stateRepository.allTaskInfos().stream().noneMatch(taskInfo -> taskInfo.getSlaveId().getValue().equals(slaveId)) && !tentativeAccept.containsKey(slaveId);
    }

    private void cleanUpTentatives(Instant now) {
        tentativeAccept.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(now))
                .map(Map.Entry::getKey)
                .peek(key -> logger.debug("removing key = " + key))
                .forEach(tentativeAccept::remove);
    }

    @EventListener
    public void onStatusUpdate(StatusUpdateEvent event) {
        if (isTerminalTaskState(event.getTaskStatus().getState())) {
            if (tentativeAccept.remove(event.getTaskStatus().getSlaveId().getValue()) != null && logger.isDebugEnabled()) {
                logger.debug("Removed tentative accept for SlaveId=" + event.getTaskStatus().getSlaveId().getValue());
            }
        }
    }

}
