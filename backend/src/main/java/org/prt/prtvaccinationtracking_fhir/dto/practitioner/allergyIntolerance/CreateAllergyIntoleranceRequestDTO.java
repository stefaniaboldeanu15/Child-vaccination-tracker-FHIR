package org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance;

import java.time.LocalDate;

public record CreateAllergyIntoleranceRequestDTO(
        String code,
        String display,
        String severity,
        LocalDate onsetDate,
        String reactionDescription
) {
}
