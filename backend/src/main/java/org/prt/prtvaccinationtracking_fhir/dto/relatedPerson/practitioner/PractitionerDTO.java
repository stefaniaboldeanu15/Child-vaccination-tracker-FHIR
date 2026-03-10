package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner;

public record PractitionerDTO(

        String id,
        String fullName,
        String identifier,   // e.g. license number
        String role

) {}