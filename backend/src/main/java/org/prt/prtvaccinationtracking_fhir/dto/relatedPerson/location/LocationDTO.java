package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location;

public record LocationDTO(
        String id,
        String name,
        String description,
        String address,
        String phone,
        String type
) {
}