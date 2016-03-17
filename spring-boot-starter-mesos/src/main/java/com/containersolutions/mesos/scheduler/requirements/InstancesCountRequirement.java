package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.InstanceCount;
import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstancesCountRequirement implements ResourceRequirement, ApplicationListener<StatusUpdateEvent> {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    Clock clock;

    @Autowired
    StateRepository stateRepository;

    @Autowired
    InstanceCount instanceCount;

    private final Map<String, Instant> tentativeAccept = new ConcurrentHashMap<>();

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        final Instant now = clock.instant();
        cleanUpTentatives(now);
        final boolean valid = totalCount() < instanceCount.getCount();
        if (valid) {
            createTentativeAccept(taskId, now);
        }
        return new OfferEvaluation(requirement, taskId, offer, valid);
    }

    private void createTentativeAccept(String taskId, Instant now) {
        logger.debug("Added tentative accept for taskId=" + taskId);
        tentativeAccept.put(taskId, now.plusSeconds(60));
        instanceCount.incrementTentative();
    }

    private void removeTentativeAccept(String taskId) {
        logger.debug("Removing tentative accept for taskId=" + taskId);
        if (tentativeAccept.remove(taskId) != null) {
            instanceCount.decrementTentative();
        } else {
            logger.warn("Tentative count is out of sync. Recounting.");
            instanceCount.setTentative(tentativeAccept.size());
        }
    }

    private int totalCount() {
        return stateRepository.allTaskInfos().size() + instanceCount.getTentative();
    }

    @Override
    public void onApplicationEvent(StatusUpdateEvent event) {
        removeTentativeAccept(event.getTaskStatus().getTaskId().getValue());
    }

    private void cleanUpTentatives(Instant now) {
        tentativeAccept.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(now))
                .map(Map.Entry::getKey)
                .forEach(this::removeTentativeAccept);
    }
}
