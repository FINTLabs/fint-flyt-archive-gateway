package no.fintlabs.template.validation;

import no.fintlabs.template.model.ElementConfig;
import no.fintlabs.template.model.ElementTemplate;
import no.fintlabs.template.model.ObjectTemplate;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.CollectionHelper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static no.fintlabs.template.validation.UniqueKeys.DUPLICATE_KEYS_REF;

public class UniqueKeysValidator implements HibernateConstraintValidator<UniqueKeys, ObjectTemplate> {

    @Override
    public boolean isValid(ObjectTemplate value, HibernateConstraintValidatorContext context) {
        List<String> duplicateKeys = findDuplicateKeys(value);
        if (duplicateKeys.isEmpty()) {
            return true;
        }
        context.addMessageParameter(
                        DUPLICATE_KEYS_REF,
                        duplicateKeys
                                .stream()
                                .map(key -> "'" + key + "'")
                                .collect(Collectors.joining(", ", "[", "]"))
                )
                .withDynamicPayload(CollectionHelper.toImmutableList(duplicateKeys));
        return false;
    }

    protected List<String> findDuplicateKeys(ObjectTemplate value) {
        Set<String> checkedKeys = new HashSet<>();
        return Stream.of(
                        value.getValueTemplates(),
                        value.getSelectableValueTemplates(),
                        value.getObjectTemplates(),
                        value.getObjectCollectionTemplates()
                )
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(ElementTemplate::getElementConfig)
                .map(ElementConfig::getKey)
                .filter(n -> !checkedKeys.add(n))
                .collect(Collectors.toList());
    }
}

