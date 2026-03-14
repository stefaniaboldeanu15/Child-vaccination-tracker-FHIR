package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.careplan;

import java.time.LocalDate;
import java.util.List;

public record CarePlanDTO(
        String id,
        String patientId,
        String title,
        String note,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        String intent,
        List<String> goalIds
) {
}
