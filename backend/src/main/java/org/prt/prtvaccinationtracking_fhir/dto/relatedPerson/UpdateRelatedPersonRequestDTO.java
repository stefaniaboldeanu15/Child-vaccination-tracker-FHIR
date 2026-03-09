package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record UpdateRelatedPersonRequestDTO(
        String relationship,
        String fullName,
        String phone,
        String email,
        String address
) {
}

