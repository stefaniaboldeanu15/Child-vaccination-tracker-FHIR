package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record RegisterPatientRequestDTO(
        String identifier,
        String firstName,
        String lastName,
        String birthDate,
        String gender
) {
}

