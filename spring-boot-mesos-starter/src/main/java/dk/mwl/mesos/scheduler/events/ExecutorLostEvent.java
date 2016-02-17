package dk.mwl.mesos.scheduler.events;

import org.apache.mesos.Protos;

public class ExecutorLostEvent extends MesosEvent {
    private final Protos.ExecutorID executorID;
    private final Protos.SlaveID slaveID;

    public ExecutorLostEvent(int status, Protos.ExecutorID executorID, Protos.SlaveID slaveID) {
        super(status);
        this.executorID = executorID;
        this.slaveID = slaveID;
    }

    public Protos.ExecutorID getExecutorID() {
        return executorID;
    }

    public Protos.SlaveID getSlaveID() {
        return slaveID;
    }
}
