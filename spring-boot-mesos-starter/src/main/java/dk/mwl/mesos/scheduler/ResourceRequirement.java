package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.function.Predicate;

public interface ResourceRequirement extends Predicate<Protos.Offer> {
}
