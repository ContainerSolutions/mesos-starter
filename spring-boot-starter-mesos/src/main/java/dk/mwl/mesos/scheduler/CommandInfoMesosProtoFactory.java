package dk.mwl.mesos.scheduler;

import dk.mwl.mesos.scheduler.config.MesosConfigProperties;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;
import java.util.stream.Collectors;

public class CommandInfoMesosProtoFactory implements MesosProtoFactory<Protos.CommandInfo.Builder> {
    @Autowired
    MesosConfigProperties mesosConfig;

    @Override
    public Protos.CommandInfo.Builder create() {
        Protos.CommandInfo.Builder builder = Protos.CommandInfo.newBuilder();
        Optional<String> command = Optional.ofNullable(mesosConfig.getCommand());
        builder.setShell(command.isPresent());
        command.ifPresent(builder::setValue);

        mesosConfig.getEnvironment().entrySet().stream()
                .map(kv -> Protos.Environment.Variable.newBuilder().setName(kv.getKey()).setValue(kv.getValue()).build())
                .collect(Collectors.collectingAndThen(
                        Collectors.toList(),
                        variables -> builder.setEnvironment(Protos.Environment.newBuilder().addAllVariables(variables))));

        return builder;
    }
}
