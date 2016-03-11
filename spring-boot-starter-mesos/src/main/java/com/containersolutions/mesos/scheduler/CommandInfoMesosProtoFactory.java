package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CommandInfoMesosProtoFactory implements MesosProtoFactory<Protos.CommandInfo.Builder> {
    @Autowired
    MesosConfigProperties mesosConfig;

    @Override
    public Protos.CommandInfo.Builder create(List<Protos.Resource> resources) {
        Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
        Optional<String> command = Optional.ofNullable(mesosConfig.getCommand());
        builder.setShell(command.isPresent());
        command.ifPresent(builder::setValue);
        builder.addAllUris(mesosConfig.getUri().stream().map(uri -> Protos.CommandInfo.URI.newBuilder().setValue(uri).build()).collect(Collectors.toList()));

        mesosConfig.getEnvironment().entrySet().stream()
                .map(kv -> Protos.Environment.Variable.newBuilder().setName(kv.getKey()).setValue(kv.getValue()).build())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        variables -> builder.setEnvironment(Protos.Environment.newBuilder().addAllVariables(variables))));

        builder.mergeEnvironment(portEnvironmentalVariables(resources));
        return builder;
    }

    private Protos.Environment portEnvironmentalVariables(List<Protos.Resource> resources) {
        Protos.Environment.Builder environment = Protos.Environment.newBuilder();
        List<Protos.Environment.Variable> portEnvVars = resources.stream()
                .filter(resource -> resource.getType().equals(Protos.Value.Type.SET))
                .filter(resource -> resource.getName().equals("ports_env"))
                .map(resource1 -> Protos.Environment.Variable.newBuilder()
                        .setName(resource1.getSet().getItem(0).split("=")[0])
                        .setValue(resource1.getSet().getItem(0).split("=")[1])
                        .build())
                .collect(Collectors.toList());
        environment.addAllVariables(portEnvVars);
        return environment.build();
    }
}
