package com.containersolutions.mesos.config.validation;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import static org.springframework.util.StringUtils.isEmpty;
import static org.springframework.validation.ValidationUtils.rejectIfEmptyOrWhitespace;

public class MesosSchedulerPropertiesValidator implements Validator {
    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == MesosConfigProperties.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        if (target instanceof MesosConfigProperties) {
            MesosConfigProperties config = (MesosConfigProperties) target;

            rejectIfEmptyOrWhitespace(errors, "master", "master.empty", "No Mesos Master set");
            rejectIfEmptyOrWhitespace(errors, "zookeeper.server", "zookeeper.server.empty", "");

            validateResources(errors, config.getResources(), isContainerized(config));
        }
    }

    private boolean isContainerized(MesosConfigProperties config) {
        return config.getDocker() != null && !isEmpty(config.getDocker().getImage());
    }

    private void validateResources(Errors errors, ResourcesConfigProperties resources, boolean containerized) {
        if (resources == null) {
            errors.rejectValue("resources", "resources.empty", "Resources are not set");
            return;
        }

        if (resources.getCount() < 0) {
            errors.rejectValue("resources.count", "resources.count.not_positive", "Count property must be a positive number");
        }

        if (resources.getCpus() <= 0.0) {
            errors.rejectValue("resources.cpus", "resources.cpus.not_positive", "cpus must be a positive number");
        }
        if (resources.getMem() <= 0.0) {
            errors.rejectValue("resources.mem", "resources.mem.not_positive", "mem must be a positive number");
        }

        resources.getPorts().entrySet().stream()
                .forEach(entry -> {
                    String name = "resources.ports." + entry.getKey();
                    if (isEmpty(entry.getValue().getHost())) {
                        errors.rejectValue("resources.ports", name + ".host.empty");
                    }
                    if (containerized && entry.getValue().getContainer() < 1) {
                        errors.rejectValue("resources.ports", name + ".container.empty");
                    }
                    if (!containerized && entry.getValue().getContainer() != 0) {
                        errors.rejectValue("resources.ports", name + ".container.not_containerized");
                    }
                });
    }
}
