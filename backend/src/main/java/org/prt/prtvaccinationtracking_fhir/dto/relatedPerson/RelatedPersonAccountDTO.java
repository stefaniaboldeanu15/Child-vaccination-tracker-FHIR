package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to display basic RelatedPerson information.
 */
public record RelatedPersonAccountDTO(
        String relatedPersonId,         /// FHIR RelatedPerson.id (technical id)
        String relatedPersonIdentifier, /// Business identifier (e.g. CNP / SSN / license)
        String firstName,
        String lastName
) {
}

