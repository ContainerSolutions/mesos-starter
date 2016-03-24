package com.containersolutions.mesos.config.validation;

import com.containersolutions.mesos.scheduler.config.MesosConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcePortConfigProperties;
import com.containersolutions.mesos.scheduler.config.ResourcesConfigProperties;
import org.springframework.util.StringUtils;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

import java.util.Map;

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

            validateResources(errors, config.getResources(), !StringUtils.isEmpty(config.getDocker().getImage()));
        }
    }

    private void validateResources(Errors errors, ResourcesConfigProperties resources, boolean containerized) {
        if (resources == null) {
            errors.rejectValue("resources", "resources.empty", "Resources are not set");
        }

        if (resources.getCpus() <= 0.0) {
            errors.rejectValue("resources.cpus", "resources.cpus.not_positive", "cpus must be a positive number");
        }
        if (resources.getMem() <= 0.0) {
            errors.rejectValue("resources.mem", "resources.mem.not_positive", "mem must be a positive number");
        }

        resources.getPorts().entrySet().forEach(entry -> {

        });


    }

    private void validateResourcesPort(Errors errors, Map.Entry<String, ResourcePortConfigProperties> entry) {
        String propKey = "resources.ports." + entry.getKey() + ".host";
        ValidationUtils.rejectIfEmptyOrWhitespace(errors, propKey, propKey + ".empty", "Port mapping for " + entry.getKey() + " does not have a host port");
    }
}
