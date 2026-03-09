package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record RelatedPersonLoginResponseDTO(
        String accessToken,
        String patientId,
        String firstName,
        String lastName
) {
}

