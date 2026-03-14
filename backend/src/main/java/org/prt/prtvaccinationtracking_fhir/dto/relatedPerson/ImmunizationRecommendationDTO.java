package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDate;

public record ImmunizationRecommendationDTO(
        String id,              // FHIR ImmunizationRecommendation.id
        String patientId,
        String vaccineCode,
        String vaccineDisplay,
        LocalDate dueDate,
        String status,          // e.g. due, overdue, completed
        String series,          // optional series name, e.g. "HepB series"
        Integer doseNumber      // optional
) {
}

