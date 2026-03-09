package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

/**
 * DTO used to create a new RelatedPerson (account).
 */
public record CreateRelatedPersonAccountRequestDTO(
        /**
         * Business identifier (e.g. username, license number)
         * Will be stored as RelatedPerson.identifier.value
         */
        String relatedPersonIdentifier,
        String firstName,
        String lastName,
        String password
) {
}

