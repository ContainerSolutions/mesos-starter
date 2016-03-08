package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.requirements.ports.PortFunction;
import com.containersolutions.mesos.scheduler.requirements.ports.PortParser;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * A requirement that parses the user's port parameters and confirms that these ports are available in the offer
 */
public class PortRequirement implements ResourceRequirement {
    public static final String MESOS_PORTS = "ports";
    public static final String PORTS_ENV_LABEL = "ports_env";
    private final MesosConfigProperties mesosConfigProperties;
    protected final Log logger = LogFactory.getLog(getClass());
    private final PortParser portParser;

    public PortRequirement(MesosConfigProperties mesosConfigProperties, PortParser portParser) {
        this.mesosConfigProperties = mesosConfigProperties;
        this.portParser = portParser;
    }

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        List<Protos.Resource> resources = new ArrayList<>();

        Set<Long> availablePorts = getAvailablePorts(offer);

        // Add ports, if available
        List<PortFunction> ports = portParser.getPorts();
        ports.forEach(starterPort -> {
            List<Protos.Port> chosenPorts = starterPort.apply(availablePorts);  // Chose port
            chosenPorts.forEach(port -> {
                resources.add(portAsResource(port.getNumber()));    // Add mesos port.
                resources.add(portEnvVarAsResource(port.getName(), port.getNumber()));  // Add entry to hold port name for env vars
                availablePorts.remove(Integer.toUnsignedLong(port.getNumber()));    // Remove chosen port from list
            });
        });

        logger.debug("Requested resources: " + portParser.toString() + ". Found and used resources: " + resources.toString().replaceAll("(?<!\\{)\\n", ", ").replaceAll("\\s+", " ").replaceAll(", (?=\\})", ""));

        return new OfferEvaluation(
                requirement,
                taskId,
                offer,
                portParser.size() == resources.size() / 2, // /2 because of extra info for env variables
                resources
        );
    }

    private Set<Long> getAvailablePorts(Protos.Offer offer) {
        return offer.getResourcesList().stream()
                .filter(resource -> resource.getName().equals("ports"))
                .flatMap(resource -> resource.getRanges().getRangeList().stream())
                .flatMapToLong(range -> LongStream.rangeClosed(range.getBegin(), range.getEnd()))
                .boxed()
                .collect(Collectors.toSet());
    }

    private Protos.Resource portAsResource(Integer port) {
        return Protos.Resource.newBuilder()
                .setType(Protos.Value.Type.RANGES)
                .setName(MESOS_PORTS)
                .setRanges(Protos.Value.Ranges.newBuilder().addRange(Protos.Value.Range.newBuilder().setBegin(port).setEnd(port)))
                .build();
    }

    private Protos.Resource portEnvVarAsResource(String name, Integer port) {
        Protos.Value.Set.Builder envText = Protos.Value.Set.newBuilder().addItem(name + "=" + port);
        return Protos.Resource.newBuilder()
                .setType(Protos.Value.Type.SET)
                .setName(PORTS_ENV_LABEL)
                .setSet(envText)
                .build();
    }
}
