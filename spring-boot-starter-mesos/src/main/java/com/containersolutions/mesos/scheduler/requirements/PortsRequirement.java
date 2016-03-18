package com.containersolutions.mesos.scheduler.requirements;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcePortConfigProperties;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.NumberUtils;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class PortsRequirement implements ResourceRequirement {
    @Autowired
    MesosConfigProperties mesosConfig;

    @Override
    public OfferEvaluation check(String requirement, String taskId, Protos.Offer offer) {
        Queue<NameAndContainerPort> unprivilegedPorts = new ConcurrentLinkedQueue<>();
        Queue<NameAndContainerPort> priviledgedPorts = new ConcurrentLinkedQueue<>();
        Map<Integer, NameAndContainerPort> fixedPorts = new HashMap<>();

        Set<Map.Entry<String, ResourcePortConfigProperties>> ports = mesosConfig.getResources().getPorts().entrySet();
        ports.forEach(kv -> {
            String hostPort = kv.getValue().getHost();
            NameAndContainerPort nameAndContainerPort = new NameAndContainerPort(kv.getKey(), kv.getValue().getContainer());
            if (hostPort.equalsIgnoreCase("ANY") || hostPort.equalsIgnoreCase("UNPRIVILEGED")) {
                unprivilegedPorts.add(nameAndContainerPort);
            } else if (hostPort.equalsIgnoreCase("PRIVILEGED")) {
                priviledgedPorts.add(nameAndContainerPort);
            } else {
                fixedPorts.put(NumberUtils.parseNumber(hostPort, Integer.class), nameAndContainerPort);
            }
        });

        List<PortMapping> portMappings = offer.getResourcesList().stream()
                .filter(resource -> resource.getName().equals("ports"))
                .flatMap(resource -> resource.getRanges().getRangeList().stream())
                .flatMapToInt(range -> IntStream.rangeClosed((int) range.getBegin(), (int) range.getEnd()))
                .sorted()
                .mapToObj(offeredPort -> {
                    if (!unprivilegedPorts.isEmpty() && offeredPort > 1024) {
                        return unprivilegedPorts.remove().toPortMapping(offeredPort);
                    } else if (!priviledgedPorts.isEmpty() && offeredPort <= 1024) {
                        return priviledgedPorts.remove().toPortMapping(offeredPort);
                    } else if (fixedPorts.containsKey(offeredPort)) {
                        return fixedPorts.remove(offeredPort).toPortMapping(offeredPort);
                    }
                    return null;
                })
                .filter(portMap -> portMap != null)
                .limit(ports.size())
                .collect(Collectors.toList());

        if (portMappings.size() == ports.size()) {
            return OfferEvaluation.accept(
                    requirement,
                    taskId,
                    offer,
                    portMappings.stream().collect(Collectors.toMap(PortMapping::envName, PortMapping::envValue)),
                    portMappings,
                    Protos.Resource.newBuilder()
                            .setType(Protos.Value.Type.RANGES)
                            .setName("ports")
                            .setRanges(Protos.Value.Ranges.newBuilder().addAllRange(
                                    portMappings.stream().map(PortMapping::toRange).collect(Collectors.toList())
                            ))
                            .build()
            );
        }
        return OfferEvaluation.decline(requirement, taskId, offer, null);
    }

    private static class NameAndContainerPort {
        String name;
        int containerPort;

        public NameAndContainerPort(String name, int containerPort) {
            this.name = name;
            this.containerPort = containerPort;
        }

        PortMapping toPortMapping(int offeredPort) {
            return new PortMapping(name, offeredPort, containerPort);
        }
    }
}
