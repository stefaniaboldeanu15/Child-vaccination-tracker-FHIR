package org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent;

import java.time.LocalDate;

public record CreateConsentRequestDTO(
        String patientId,
        String relatedPersonId,
        String scope,          // vaccination
        String status,         // active, rejected, withdrawn
        LocalDate dateGiven
) {
}
