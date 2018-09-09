package com.containersolutions.mesos.scheduler;

import com.containersolutions.mesos.scheduler.events.FrameworkRemovedEvent;
import com.containersolutions.mesos.scheduler.events.TearDownFrameworkEvent;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.event.EventListener;

public class TeardownManager implements ApplicationContextAware {
    protected final Log logger = LogFactory.getLog(getClass());
    private ApplicationContext applicationContext;

    @EventListener
    public void onFrameworkRemovedEvent(FrameworkRemovedEvent event) {
        logger.info("Framework removed. Shutting down scheduler, reason=\"" + event.getSource() + "\"");
        System.exit(SpringApplication.exit(applicationContext, () -> 10));
    }
    @EventListener
    public void onTearDownFrameworkEvent(TearDownFrameworkEvent event) {
        logger.info("Framework shutting down");
        final Thread shutdownTask = new Thread(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.exit(SpringApplication.exit(applicationContext, () -> 0));
        });
        shutdownTask.setDaemon(true);
        shutdownTask.start();
    }

    public void teardownFramework() {
        System.exit(SpringApplication.exit(applicationContext, () -> 10));
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}
