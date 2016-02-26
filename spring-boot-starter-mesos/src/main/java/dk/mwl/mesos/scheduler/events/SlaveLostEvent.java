package dk.mwl.mesos.scheduler.events;

import org.apache.mesos.Protos;

public class SlaveLostEvent extends MesosEvent {
    public SlaveLostEvent(Protos.SlaveID slaveID) {
        super(slaveID);
    }
}
