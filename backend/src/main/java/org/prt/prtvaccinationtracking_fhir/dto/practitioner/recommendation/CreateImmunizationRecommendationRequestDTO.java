package org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation;

import java.time.LocalDate;

public record CreateImmunizationRecommendationRequestDTO(
        String patientId,
        String vaccineCode,
        String vaccineDisplay,
        String status,  // due, completed, overdue
        LocalDate dueDate,
        String recommendationSource

) {
}
