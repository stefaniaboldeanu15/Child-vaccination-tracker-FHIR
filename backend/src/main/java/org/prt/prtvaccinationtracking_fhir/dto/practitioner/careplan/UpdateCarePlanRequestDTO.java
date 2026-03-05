package org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan;

import java.time.LocalDate;

public record UpdateCarePlanRequestDTO(
        String title,
        String description,
        LocalDate endDate,
        String status
) {
}
