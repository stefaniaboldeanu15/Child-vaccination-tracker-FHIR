package org.prt.prtvaccinationtracking_fhir.auth.dto;

public record RegisterRelatedPersonRequest(
        String username,
        String password,
        String patientId,
        String firstName,
        String lastName,
        String relationship,
        String phone,
        String email,
        String address
) {
}
