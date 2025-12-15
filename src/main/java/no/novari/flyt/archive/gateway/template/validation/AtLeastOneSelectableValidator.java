package no.novari.flyt.archive.gateway.template.validation;

import no.novari.flyt.archive.gateway.template.model.SelectableValueTemplate;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;

public class AtLeastOneSelectableValidator implements HibernateConstraintValidator<AtLeastOneSelectable, SelectableValueTemplate> {

    @Override
    public boolean isValid(SelectableValueTemplate value, HibernateConstraintValidatorContext hibernateConstraintValidatorContext) {
        return value == null || getNumberOfSelectablesAndSelectablesSources(value) > 0;
    }

    private int getNumberOfSelectablesAndSelectablesSources(SelectableValueTemplate value) {
        int numberOfSelectables = value.getSelectables() == null ? 0 : value.getSelectables().size();
        int numberOfSelectablesSources = value.getSelectablesSources() == null ? 0 : value.getSelectablesSources().size();
        return numberOfSelectables + numberOfSelectablesSources;
    }

}
