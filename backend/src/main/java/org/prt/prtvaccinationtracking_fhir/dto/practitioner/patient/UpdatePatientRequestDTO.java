package org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient;


public record UpdatePatientRequestDTO(

        String firstName,
        String lastName,
        String phone,
        String email,
        String address

) {}