package org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal;

import java.time.LocalDate;

public record GoalDTO(
        String id,
        String patientId,
        String carePlanId,
        String description,
        LocalDate targetDate,
        String status
) {
}
