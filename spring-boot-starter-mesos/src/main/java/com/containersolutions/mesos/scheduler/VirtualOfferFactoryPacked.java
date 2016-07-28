package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.mesos.Protos;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.lang.Double.doubleToLongBits;
import static java.lang.Double.longBitsToDouble;

public class VirtualOfferFactoryPacked implements VirtualOfferFactory {
    protected final Log logger = LogFactory.getLog(getClass());

    MesosConfigProperties configuration;

    @Autowired
    public VirtualOfferFactoryPacked(MesosConfigProperties configuration) {
        this.configuration = configuration;
    }

    @Override
    public Stream<VirtualOffer> slice(Protos.Offer offer) {
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(new Iterator<VirtualOffer>() {
            final AtomicLong remainingCpus = new AtomicLong(doubleToLongBits(offer.getResourcesList().stream().filter(resource -> resource.getName().equalsIgnoreCase("cpus")).findFirst().orElseThrow(() -> new IllegalStateException("Offer has no CPUs")).getScalar().getValue()));
            final AtomicLong remainingMem = new AtomicLong(doubleToLongBits(offer.getResourcesList().stream().filter(resource -> resource.getName().equalsIgnoreCase("mem")).findFirst().orElseThrow(() -> new IllegalStateException("Offer has no Mem")).getScalar().getValue()));
            final double requiredCpus = configuration.getResources().getCpus();
            final double requiredMem = configuration.getResources().getMem();

            @Override
            public boolean hasNext() {
                return hasCpus() && hasMem();
            }

            private boolean hasCpus() {
                return longBitsToDouble(remainingCpus.get()) >= requiredCpus;
            }

            private boolean hasMem() {
                return longBitsToDouble(remainingMem.get()) >= requiredMem;
            }

            @Override
            public VirtualOffer next() {
                double cpus = longBitsToDouble(remainingCpus.get());
                remainingCpus.compareAndSet(doubleToLongBits(cpus), doubleToLongBits(cpus - requiredCpus));
                double mem = longBitsToDouble(remainingMem.get());
                remainingMem.compareAndSet(doubleToLongBits(mem), doubleToLongBits(mem - requiredMem));

                logger.debug("Issuing VOffer");
                return new VirtualOffer(offer);
            }
        }, Spliterator.NONNULL), false);
    }
}
