package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition;

public record UpdateConditionRequestDTO(
        String clinicalStatus,
        String verificationStatus,
        String notes
) {
}