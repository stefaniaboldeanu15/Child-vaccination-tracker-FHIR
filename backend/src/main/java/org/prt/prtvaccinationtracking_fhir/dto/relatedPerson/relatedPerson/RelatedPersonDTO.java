package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson
;

public record RelatedPersonDTO(

        String id,
        String fullName,
        String relationship,   // mother / father / guardian
        String phone,
        String email

) {}