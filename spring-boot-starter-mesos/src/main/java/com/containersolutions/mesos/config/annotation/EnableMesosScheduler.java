package com.containersolutions.mesos.config.annotation;

import com.containersolutions.mesos.config.autoconfigure.MesosSchedulerConfiguration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Import(MesosSchedulerConfiguration.class)
public @interface EnableMesosScheduler {
}
