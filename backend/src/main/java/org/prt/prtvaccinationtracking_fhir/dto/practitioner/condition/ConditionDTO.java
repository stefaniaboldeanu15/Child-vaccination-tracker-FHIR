package org.prt.prtvaccinationtracking_fhir.dto.practitioner.condition;

import java.time.LocalDate;

public record ConditionDTO(
        String id,
        String patientId,
        String code,
        String display,
        String clinicalStatus,
        String verificationStatus,
        LocalDate onsetDate,
        String recorderName,
        String notes
) {
}
