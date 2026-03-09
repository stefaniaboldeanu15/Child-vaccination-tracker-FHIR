package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * Data Transfer Object for returning key related person details after successful login.
 */
public record RelatedPersonHeaderDTO(
        Long id,
        String fullName,
        String organizationName
) {
}

