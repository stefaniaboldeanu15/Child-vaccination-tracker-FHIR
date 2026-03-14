package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * Data Transfer Object for the API request during login (identifier and password).
 */
public record LoginRequestDTO(
        String identifier, // SSN or other related person identifier
        String password
) {
}

