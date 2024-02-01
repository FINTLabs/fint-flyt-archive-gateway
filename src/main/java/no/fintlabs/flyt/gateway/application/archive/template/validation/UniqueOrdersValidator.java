package no.fintlabs.flyt.gateway.application.archive.template.validation;

import no.fintlabs.flyt.gateway.application.archive.template.model.ElementTemplate;
import no.fintlabs.flyt.gateway.application.archive.template.model.ObjectTemplate;
import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.hibernate.validator.internal.util.CollectionHelper;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UniqueOrdersValidator implements HibernateConstraintValidator<UniqueOrders, ObjectTemplate> {

    @Override
    public boolean isValid(ObjectTemplate value, HibernateConstraintValidatorContext context) {
        List<Integer> duplicateOrders = findDuplicateOrders(value);
        if (duplicateOrders.isEmpty()) {
            return true;
        }
        context.addMessageParameter(
                        UniqueOrders.DUPLICATE_ORDERS_REF,
                        duplicateOrders
                                .stream()
                                .map(key -> "'" + key + "'")
                                .collect(Collectors.joining(", ", "[", "]"))
                )
                .withDynamicPayload(CollectionHelper.toImmutableList(duplicateOrders));
        return false;
    }

    protected List<Integer> findDuplicateOrders(ObjectTemplate value) {
        Set<Integer> checkedOrders = new HashSet<>();
        return Stream.of(
                        value.getValueTemplates(),
                        value.getSelectableValueTemplates(),
                        value.getObjectTemplates(),
                        value.getObjectCollectionTemplates()
                )
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(ElementTemplate::getOrder)
                .filter(n -> !checkedOrders.add(n))
                .collect(Collectors.toList());
    }
}

