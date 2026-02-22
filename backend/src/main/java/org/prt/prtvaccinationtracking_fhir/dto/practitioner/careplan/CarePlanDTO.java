package org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan;

import java.time.LocalDate;
import java.util.List;

public record CarePlanDTO(
        String id,
        String patientId,
        String title,
        String description,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        List<String> goalIds
) {
}
