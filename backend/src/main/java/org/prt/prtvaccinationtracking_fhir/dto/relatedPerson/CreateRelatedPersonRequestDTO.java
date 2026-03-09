package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record CreateRelatedPersonRequestDTO(
        String patientId,              // CHILD Patient.id
        String relatedPersonIdentifier,
        String relationship,           // mother / father / guardian
        String fullName,
        String phone,
        String email,
        String address
) {
}

