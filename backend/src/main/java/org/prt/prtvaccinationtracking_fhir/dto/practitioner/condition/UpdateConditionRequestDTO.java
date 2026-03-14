package org.prt.prtvaccinationtracking_fhir.dto.practitioner.condition;

public record UpdateConditionRequestDTO(
        String clinicalStatus,
        String verificationStatus,
        String notes
) {
}
