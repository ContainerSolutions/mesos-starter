package dk.mwl.mesos.scheduler;

import org.apache.mesos.Protos;

import java.util.List;

import static java.util.Arrays.asList;

public class OfferEvaluation {
    Protos.Offer offer;
    List<Protos.Resource> resources;
    boolean valid;

    public OfferEvaluation(Protos.Offer offer, boolean valid, List<Protos.Resource> resources) {
        this.offer = offer;
        this.resources = resources;
        this.valid = valid;
    }

    public OfferEvaluation(Protos.Offer offer, boolean valid, Protos.Resource ... resources) {
        this(offer, valid, asList(resources));
    }

    public boolean isValid() {
        return valid;
    }

    public Protos.Offer getOffer() {
        return offer;
    }

    public List<Protos.Resource> getResources() {
        return resources;
    }
}
