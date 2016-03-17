package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.events.StatusUpdateEvent;
import com.containersolutions.mesos.scheduler.state.StateRepository;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.jmx.export.annotation.ManagedAttribute;
import org.springframework.jmx.export.annotation.ManagedResource;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@ManagedResource
public class InstancesCountRequirement implements ResourceRequirement, ApplicationListener<StatusUpdateEvent> {
    private final Log logger = LogFactory.getLog(getClass());

    @Autowired
    Clock clock;

    @Autowired
    StateRepository stateRepository;

    private final AtomicInteger count = new AtomicInteger(1);

    private final Map<String, Instant> tentativeAccept = new ConcurrentHashMap<>();

    public InstancesCountRequirement(int count) {
        this.count.set(count);
    }

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        final Instant now = clock.instant();
        cleanUpTentatives(now);
        final boolean valid = getTotalCount() < count.get();
        if (valid) {
            tentativeAccept.put(taskId, now.plusSeconds(60));
        }
        return new OfferEvaluation(requirement, taskId, offer, valid);
    }

    @ManagedAttribute
    public int getTotalCount() {
        return stateRepository.allTaskInfos().size() + tentativeAccept.size();
    }

    @Override
    public void onApplicationEvent(StatusUpdateEvent event) {
        tentativeAccept.remove(event.getTaskStatus().getTaskId().getValue());
    }

    private void cleanUpTentatives(Instant now) {
        tentativeAccept.entrySet().stream()
                .filter(entry -> entry.getValue().isBefore(now))
                .map(Map.Entry::getKey)
                .peek(key -> logger.debug("removing key = " + key))
                .forEach(tentativeAccept::remove);
    }

    @ManagedAttribute
    public void setCount(int count) {
        this.count.set(count);
    }

    @ManagedAttribute
    public int getCount() {
        return count.get();
    }
}
