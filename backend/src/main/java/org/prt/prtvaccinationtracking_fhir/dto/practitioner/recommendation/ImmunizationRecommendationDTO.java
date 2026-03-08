package org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation;

import java.time.LocalDate;

public record ImmunizationRecommendationDTO(
        String id,
        String patientId,
        String vaccineCode,
        String vaccineDisplay,
        String status,        // due, completed, overdue
        LocalDate dueDate,
        String recommendationSource  //  Austrian pediatric schedule
) {}
