package org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation;

import java.time.LocalDate;

public record CreateImmunizationRecommendationRequestDTO(
        String vaccineCode,
        String vaccineDisplay,
        LocalDate dueDate,
        String status   // due, completed, overdue
) {
}
