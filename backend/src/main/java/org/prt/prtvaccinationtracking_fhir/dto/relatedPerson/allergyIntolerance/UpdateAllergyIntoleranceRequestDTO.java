package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance;

public record UpdateAllergyIntoleranceRequestDTO(
        AllergyIntoleranceDTO.AllergyClinicalStatus clinicalStatus,
        AllergyIntoleranceDTO.AllergyCriticality criticality
) {}