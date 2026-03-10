package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization;

public record UpdateImmunizationRequestDTO(
        String lotNumber,
        String site,
        ImmunizationStatusDTO status,
        String notes
) {}