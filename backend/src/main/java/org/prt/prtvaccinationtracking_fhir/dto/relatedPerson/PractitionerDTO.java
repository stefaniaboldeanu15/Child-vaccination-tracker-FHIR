package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to display basic Practitioner (healthcare provider) information.
 * Used e.g. for the performer of an immunization.
 */
public record PractitionerDTO(
        String practitionerId,         /// FHIR Practitioner.id (technical id)
        String practitionerIdentifier, /// Business identifier (e.g. CNP / SSN / license)
        String firstName,
        String lastName
) {
}

