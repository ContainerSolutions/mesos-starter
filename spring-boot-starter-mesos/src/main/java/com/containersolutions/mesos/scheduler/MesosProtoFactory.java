package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.List;

public interface MesosProtoFactory<T> {
    T create(List<Protos.Resource> resources);
}
