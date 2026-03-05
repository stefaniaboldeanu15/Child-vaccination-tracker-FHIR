package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record CreateRelatedPersonRequestDTO(
        String identifier,
        String firstName,
        String lastName,
        String relationship,
        String phone,
        String email,
        String address,
        String patientId
) {
}


