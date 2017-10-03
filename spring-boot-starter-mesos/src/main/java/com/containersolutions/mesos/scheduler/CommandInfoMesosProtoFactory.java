package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandInfoMesosProtoFactory implements MesosProtoFactory<Protos.CommandInfo,Map<String, String>> {
    private final MesosConfigProperties mesosConfig;

    public CommandInfoMesosProtoFactory(MesosConfigProperties mesosConfig) {
        this.mesosConfig = mesosConfig;
    }

    @Override
    public Protos.CommandInfo create(Map<String, String> additionalEnvironmentVariables) {
        Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
        Optional<String> command = Optional.ofNullable(mesosConfig.getCommand());
        builder.setShell(command.isPresent());
        command.ifPresent(builder::setValue);
        builder.addAllUris(mesosConfig.getUri().stream().map(uri -> Protos.CommandInfo.URI.newBuilder().setValue(uri).build()).collect(Collectors.toList()));

        Map<String, String> environmentVariables = new LinkedHashMap<>();
        environmentVariables.putAll(additionalEnvironmentVariables);
        environmentVariables.putAll(mesosConfig.getEnvironment());

        //todo: migrate to foreach
        environmentVariables.entrySet().stream()
                .map(kv -> Protos.Environment.Variable.newBuilder().setName(kv.getKey()).setValue(kv.getValue()).build())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        variables -> builder.setEnvironment(Protos.Environment.newBuilder().addAllVariables(variables))));
        return builder.build();
    }
}
