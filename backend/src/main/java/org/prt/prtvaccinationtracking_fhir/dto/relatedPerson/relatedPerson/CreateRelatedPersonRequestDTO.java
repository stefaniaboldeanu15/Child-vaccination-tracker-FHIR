package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record CreateRelatedPersonRequestDTO(
        String firstName,
        String lastName,
        String relationship,   // mother / father / guardian
        String phone,
        String email,
        String address
) {}