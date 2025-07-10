package no.fintlabs.flyt.gateway.application.archive.resource.configuration.validation;


import no.fintlabs.flyt.gateway.application.archive.resource.configuration.ResourcePublishingRefreshConfigurationProperties;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class FromTimeOfDayBeforeOrSameAsToTimeOfDayValidator implements
        ConstraintValidator<FromTimeOfDayBeforeOrSameAsToTimeOfDay, ResourcePublishingRefreshConfigurationProperties> {

    @Override
    public boolean isValid(ResourcePublishingRefreshConfigurationProperties value, ConstraintValidatorContext context) {
        return !value.getFromTimeOfDay().isAfter(value.getToTimeOfDay());
    }
}
