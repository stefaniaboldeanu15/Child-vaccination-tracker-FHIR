package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance;

public record CreateAllergyIntoleranceRequestDTO(
        String patientId,
        AllergyIntoleranceDTO.AllergyClinicalStatus clinicalStatus,
        AllergyIntoleranceDTO.AllergyType type,
        AllergyIntoleranceDTO.AllergyIntoleranceCategory category,
        AllergyIntoleranceDTO.AllergyCriticality criticality,
        String code,
        String encounterId
) {
}
