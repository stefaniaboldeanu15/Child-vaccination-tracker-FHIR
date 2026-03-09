package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record RegistrationRequestDTO(
        String identifier,
        String password,
        String firstName,
        String lastName
) {
}

