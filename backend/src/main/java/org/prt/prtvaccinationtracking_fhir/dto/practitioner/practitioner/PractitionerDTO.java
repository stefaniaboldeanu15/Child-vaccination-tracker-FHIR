package org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner;

public record PractitionerDTO(

        String id,
        String fullName,
        String identifier,   // e.g. license number
        String role

) {}