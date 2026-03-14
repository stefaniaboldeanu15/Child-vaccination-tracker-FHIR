package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.patient;

public record UpdatePatientRequestDTO(
        String firstName,
        String lastName,
        String phone,
        String email,
        String address
) {}