package org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.RelatedPersonDTO;

import java.time.LocalDate;

public record PatientDetailsDTO(

        String id,
        String svnr,
        String firstName,
        String lastName,
        String fullName,
        LocalDate birthDate,
        String gender,
        String phone,
        String email,
        String address,
        RelatedPersonDTO parent

) {}