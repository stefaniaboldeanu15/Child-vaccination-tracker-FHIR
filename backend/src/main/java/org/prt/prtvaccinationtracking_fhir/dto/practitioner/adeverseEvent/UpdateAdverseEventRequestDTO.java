package org.prt.prtvaccinationtracking_fhir.dto.practitioner.adeverseEvent;

public record UpdateAdverseEventRequestDTO(

        CreateAdverseEventRequestDTO.Severity severity,
        String outcome,
        String notes

) {}
