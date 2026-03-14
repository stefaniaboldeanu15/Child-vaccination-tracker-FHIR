package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDate;

public record CreateImmunizationRecommendationRequest(
        String vaccineCode,     // required
        String vaccineDisplay,  // optional
        LocalDate dueDate,      // required
        String series,          // optional
        Integer doseNumber,     // optional
        String notes            // optional free text
) {
}

