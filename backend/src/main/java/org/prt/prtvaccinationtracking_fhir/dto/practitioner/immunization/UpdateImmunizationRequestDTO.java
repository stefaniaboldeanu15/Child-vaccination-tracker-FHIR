package org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization;

public record UpdateImmunizationRequestDTO(
        String lotNumber,
        String site,
        ImmunizationStatusDTO status,
        String notes
) {}