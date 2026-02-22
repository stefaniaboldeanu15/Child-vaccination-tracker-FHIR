package org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal;

import java.time.LocalDate;

public record UpdateGoalRequestDTO(
        String description,
        LocalDate targetDate,
        String status
) {
}
