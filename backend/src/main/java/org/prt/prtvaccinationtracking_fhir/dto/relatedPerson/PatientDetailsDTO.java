package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record PatientDetailsDTO(
        String patientId,        /// FHIR server id (Patient.id)
        String patientIdentifier,/// Business identifier (Austrian social security number)
        String firstName,
        String lastName,
        String birthDate,
        String gender
) {
}

