package no.novari.flyt.archive.gateway.resource.web.exceptions;

import java.util.List;

public class SearchKlasseOrderNotFoundInCaseException extends RuntimeException {
    public SearchKlasseOrderNotFoundInCaseException(
            List<Integer> caseKlasseOrders,
            Integer searchKlasseOrder
    ) {
        super(String.format(
                "Could not find search klasse order=%d in case klasse orders=%s",
                searchKlasseOrder,
                caseKlasseOrders
        ));
    }
}
