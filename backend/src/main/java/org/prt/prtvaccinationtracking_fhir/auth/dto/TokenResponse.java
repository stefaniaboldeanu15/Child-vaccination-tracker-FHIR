package org.prt.prtvaccinationtracking_fhir.auth.dto;

public record TokenResponse(
        String access_token,
        String token_type,
        long expires_in,
        String scope,
        String patient,
        String fhirUser
) {
}
