package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson;

public record RelatedPersonDTO(
        String id,
        String fullName,
        String identifier,
        String relationship,
        String phone,
        String email,
        String address,
        String patientId
) {
}


