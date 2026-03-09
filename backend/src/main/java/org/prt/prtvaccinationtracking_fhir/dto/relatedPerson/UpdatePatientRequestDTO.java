package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record UpdatePatientRequestDTO(
        String patientIdentifier, // SSN
        String firstName,
        String lastName,
        String birthDate,         // yyyy-MM-dd
        String gender
) {
}

