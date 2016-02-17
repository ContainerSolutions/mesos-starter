package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.List;

public interface TaskInfoFactory {

    Protos.TaskInfo create(Protos.Offer offer, List<Protos.Resource> resources);
}
