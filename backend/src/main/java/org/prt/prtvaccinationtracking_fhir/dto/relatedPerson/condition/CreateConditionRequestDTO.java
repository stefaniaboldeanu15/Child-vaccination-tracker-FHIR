package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition;

import java.time.LocalDate;

public record CreateConditionRequestDTO(
        String code,
        String display,
        String clinicalStatus,   // active, resolved, inactive
        String verificationStatus, // confirmed, suspected
        LocalDate onsetDate,
        String notes
) {
}