// backend/src/main/java/org/prt/prtvaccinationtracking_fhir/dto/relatedPerson/patient/PatientDetailsDTO.java
package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.patient;

import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonDTO;

import java.time.LocalDate;

public record PatientDetailsDTO(
        String id,
        String svnr,
        String firstName,
        String lastName,
        String fullName,
        LocalDate birthDate,
        CreatePatientRequestDTO.Gender gender,
        String phone,
        String email,
        String address,
        RelatedPersonDTO parent
) {
}
