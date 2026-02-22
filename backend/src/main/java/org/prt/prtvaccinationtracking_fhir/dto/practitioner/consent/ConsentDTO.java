package org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent;

import java.time.LocalDate;

public record ConsentDTO(
        String id,
        String patientId,
        String relatedPersonId,
        String scope,
        String status,
        LocalDate dateGiven,
        String recorderName
) {
}
