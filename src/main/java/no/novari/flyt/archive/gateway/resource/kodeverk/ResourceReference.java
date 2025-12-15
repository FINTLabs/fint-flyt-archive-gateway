package no.novari.flyt.archive.gateway.resource.kodeverk;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;


@AllArgsConstructor
@Getter
@EqualsAndHashCode
public class ResourceReference {

    private final String id;
    private final String displayName;

}
