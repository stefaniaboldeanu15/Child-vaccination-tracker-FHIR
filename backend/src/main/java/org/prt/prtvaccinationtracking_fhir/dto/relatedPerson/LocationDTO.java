package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record LocationDTO(
        String locationId,
        String organizationId,
        String name,
        String address,
        String description
) {
}

