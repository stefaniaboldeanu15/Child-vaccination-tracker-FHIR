package org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan;

import java.time.LocalDate;

public record CreateCarePlanRequestDTO(
        String patientId,
        String title,              // e.g. "Austrian Pediatric Vaccination Plan"
        String note,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
}
