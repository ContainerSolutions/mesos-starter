package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Takes offered and requested ports, and merges them into a list of valid resources.
 */
public class PortMerger {
    protected final Log logger = LogFactory.getLog(getClass());
    private final List<PortPicker.PortResourceMapper> resourceMappers;

    @Autowired
    public PortMerger(List<PortPicker.PortResourceMapper> resourceMappers) {
        this.resourceMappers = resourceMappers;
    }

    public List<Protos.Resource> merge(Set<Integer> offeredPorts, List<PortFactory.PortFunction> requestedPorts) {
        List<Protos.Resource> resources = new ArrayList<>();
        requestedPorts.stream()
                .map(portFunction -> portFunction.apply(offeredPorts))
                .flatMap(Collection::stream)
                .forEach(port -> { // Must do this sequentially, so subsequent ports don't take the same port as last time.
                    resources.addAll(mapToResource(port));  // Map port to all resources
                    offeredPorts.remove(port.getNumber());    // Remove chosen port from available list
                });
        return resources;
    }

    public Boolean allPortsMerged(Set<Integer> offeredPorts, List<PortFactory.PortFunction> requestedPorts) {
        List<Protos.Resource> mergedPorts = merge(offeredPorts, requestedPorts);
        logger.debug("Resource mapper size " + resourceMappers.size() +
                        ", requested ports size " + requestedPorts.size() +
                        ", merged ports size " + mergedPorts.size()
        );
        return resourceMappers.size() > 0 ?
                requestedPorts.size() == mergedPorts.size() / resourceMappers.size() :
                requestedPorts.size() == mergedPorts.size();
    }

    private List<Protos.Resource> mapToResource(Protos.Port port) {
        return resourceMappers.stream().map(mapper -> mapper.apply(port)).collect(Collectors.toList());
    }
}
