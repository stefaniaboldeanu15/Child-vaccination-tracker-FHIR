package org.prt.prtvaccinationtracking_fhir.auth.dto;

public record RegisterPractitionerRequest(
        String username,
        String password,
        String identifier,      // license number
        String firstName,
        String lastName,
        String specialization,
        String phone,
        String email,
        String organizationId
) {
}
