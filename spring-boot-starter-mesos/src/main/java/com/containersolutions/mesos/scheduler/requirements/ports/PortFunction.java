package com.containersolutions.mesos.scheduler.requirements.ports;

import org.apache.mesos.Protos;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

public interface PortFunction extends Function<Set<Long>, List<Protos.Port>> {
}
