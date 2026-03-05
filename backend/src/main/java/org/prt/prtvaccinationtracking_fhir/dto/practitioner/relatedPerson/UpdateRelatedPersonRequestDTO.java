package org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson
;

public record UpdateRelatedPersonRequestDTO(

        String phone,
        String email,
        String address

) {}