package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortPicker {
    protected final Log logger = LogFactory.getLog(getClass());
    protected final PortParser portParser;
    private final PortMerger portMerger;

    @Autowired
    public PortPicker(PortParser portParser, PortMerger portMerger) {
        this.portParser = portParser;
        this.portMerger = portMerger;
    }

    public Boolean isValid(Set<Integer> offeredPorts) {
        Boolean valid = portMerger.allPortsMerged(offeredPorts, portParser.getPorts());
        logger.debug("Offer is " + (valid ? "valid" : "not valid"));
        return valid;
    }

    public List<Protos.Resource> getResources(Set<Integer> offeredPorts) {
        List<PortFactory.PortFunction> requestedPorts = portParser.getPorts(); // Get requested ports
        List<Protos.Resource> resources = portMerger.merge(offeredPorts, requestedPorts);
        logger.debug("Requested resources: " + requestedPorts.toString() + ". Found and used resources: " + resources.toString().replaceAll("(?<!\\{)\\n", ", ").replaceAll("\\s+", " ").replaceAll(", (?=\\})", ""));
        return resources;
    }

    public static Set<Integer> toPortSet(Protos.Offer offer) {
        return offer.getResourcesList().stream()
                .filter(resource -> resource.getName().equals("ports"))
                .flatMap(resource -> resource.getRanges().getRangeList().stream())
                .flatMapToInt(range -> IntStream.rangeClosed((int) range.getBegin(), (int) range.getEnd()))
                .boxed()
                .collect(Collectors.toSet());
    }

    public interface PortResourceMapper extends Function<Protos.Port, Protos.Resource> {
    }
}
