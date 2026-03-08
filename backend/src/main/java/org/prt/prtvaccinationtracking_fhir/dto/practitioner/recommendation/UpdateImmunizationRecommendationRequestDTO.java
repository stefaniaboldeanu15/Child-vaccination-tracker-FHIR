package org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation;

import java.time.LocalDate;

public record UpdateImmunizationRecommendationRequestDTO(
        LocalDate dueDate,
        String status
) {
}
