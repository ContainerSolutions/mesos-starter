package com.containersolutions.mesos.scheduler;

public interface MesosProtoFactory<T, A> {
    T create(A argument);
}
