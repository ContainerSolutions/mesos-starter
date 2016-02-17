package dk.mwl.mesos.scheduler.events;

import org.apache.mesos.Protos;

public class FrameworkMessageEvent extends MesosEvent {
    private final Protos.ExecutorID executorID;
    private final Protos.SlaveID slaveID;

    /**
     * Create a new ApplicationEvent.
     *
     * @param data the component that published the event (never {@code null})
     * @param executorID
     * @param slaveID
     */
    public FrameworkMessageEvent(byte[] data, Protos.ExecutorID executorID, Protos.SlaveID slaveID) {
        super(data);
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
