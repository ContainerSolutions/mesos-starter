package dk.mwl.mesos.scheduler.requirements;

import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.stream.Collectors;

public class RoleRequirement implements ResourceRequirement {
    @Value("${mesos.role}")
    String role;

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        List<Protos.Resource> roleResources = offer.getResourcesList().stream()
                .filter(Protos.Resource::hasRole)
                .filter(resource -> resource.getRole().equals(role))
                .collect(Collectors.toList());
        return new OfferEvaluation(
                requirement, taskId, offer,
                !roleResources.isEmpty(),
                roleResources);
    }
}
