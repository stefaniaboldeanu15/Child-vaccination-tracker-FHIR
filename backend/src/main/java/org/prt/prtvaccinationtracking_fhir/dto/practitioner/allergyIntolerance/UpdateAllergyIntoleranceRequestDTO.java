package org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance;

public record UpdateAllergyIntoleranceRequestDTO(
        AllergyIntoleranceDTO.AllergyClinicalStatus clinicalStatus,
        AllergyIntoleranceDTO.AllergyCriticality criticality
) {}