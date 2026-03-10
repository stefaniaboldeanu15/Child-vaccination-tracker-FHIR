package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner;

public record CreatePractitionerRequestDTO(
        String identifier,      // license number
        String firstName,
        String lastName,
        String specialization,
        String phone,
        String email,
        String organizationId
) {
}
