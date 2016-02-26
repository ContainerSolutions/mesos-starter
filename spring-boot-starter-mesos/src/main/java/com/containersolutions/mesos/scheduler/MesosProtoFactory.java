package com.containersolutions.mesos.scheduler;

public interface MesosProtoFactory<T> {
    T create();
}
