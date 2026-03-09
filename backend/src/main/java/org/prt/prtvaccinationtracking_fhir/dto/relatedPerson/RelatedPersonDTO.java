package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record RelatedPersonDTO(
        String relatedPersonId,
        String relatedPersonIdentifier, // e.g. national ID, guardian ID
        String relationship,            // "mother", "father", "guardian"
        String fullName,
        String phone,
        String email,
        String address
) {
}

