package org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient;

import java.time.LocalDate;

public record CreatePatientRequestDTO(
        String svnr,
        String firstName,
        String lastName,
        LocalDate birthDate,
        Gender gender
) {
    public enum Gender {
        male,
        female,
        other,
        unknown
    }
}