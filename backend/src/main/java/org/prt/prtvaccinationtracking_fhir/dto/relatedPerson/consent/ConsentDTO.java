package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent;

import java.time.LocalDate;

public record ConsentDTO(
        String id,
        String patientId,
        String relatedPersonId,
        String scope,
        String status,
        LocalDate dateGiven
) {
}