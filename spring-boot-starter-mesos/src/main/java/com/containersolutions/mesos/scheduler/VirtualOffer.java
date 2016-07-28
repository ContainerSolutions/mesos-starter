package com.containersolutions.mesos.scheduler;

import org.apache.mesos.Protos;

public class VirtualOffer {
    private Protos.Offer parent;

    public VirtualOffer(Protos.Offer parent) {
        this.parent = parent;
    }

    public Protos.Offer getParent() {
        return parent;
    }
}
