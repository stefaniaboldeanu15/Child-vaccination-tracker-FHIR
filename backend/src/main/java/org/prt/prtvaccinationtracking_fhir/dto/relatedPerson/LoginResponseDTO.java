package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * Data Transfer Object for the API response after a successful login attempt.
 * Contains the generated access token and the high-level details of the authenticated related person.
 */
public record LoginResponseDTO(
        String accessToken,
        RelatedPersonAccountDTO relatedPerson
) {
}

