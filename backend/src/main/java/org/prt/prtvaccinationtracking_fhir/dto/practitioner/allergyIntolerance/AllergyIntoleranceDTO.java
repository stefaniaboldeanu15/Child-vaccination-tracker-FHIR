package org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance;

import java.time.LocalDate;

public record AllergyIntoleranceDTO(
        String id,
        String patientId,
        String code,
        String display,
        String clinicalStatus,   // active, resolved
        String severity,         // mild, moderate, severe
        LocalDate onsetDate,
        String reactionDescription,
        String recorderName
) {
}
