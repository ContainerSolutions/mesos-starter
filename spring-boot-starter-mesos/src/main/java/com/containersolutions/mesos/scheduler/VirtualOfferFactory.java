package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.stream.Stream;

public interface VirtualOfferFactory {
    Stream<VirtualOffer> slice(Protos.Offer offer);
}
