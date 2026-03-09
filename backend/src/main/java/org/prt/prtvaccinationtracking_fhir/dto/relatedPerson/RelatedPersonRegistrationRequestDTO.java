package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record RelatedPersonRegistrationRequestDTO(
        String username,
        String password,
        String firstName,
        String lastName,
        String birthDate // format: YYYY-MM-DD
) {
}

