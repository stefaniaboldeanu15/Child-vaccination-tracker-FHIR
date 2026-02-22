package org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization;

import java.time.LocalDate;

public record CreateImmunizationRequestDTO(

        String vaccineCode,        // CVX code
        String vaccineDisplay,     // Human readable name
        LocalDate administrationDate,
        String lotNumber,
        String site,               // left arm, right thigh, etc.
        Integer doseNumber,
        String encounterId         //  link to encounter

) {}