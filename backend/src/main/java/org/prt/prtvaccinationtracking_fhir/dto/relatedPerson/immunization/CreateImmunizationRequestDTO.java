package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization;

import java.time.LocalDate;

public record CreateImmunizationRequestDTO(
        String patientId,
        ImmunizationStatusDTO status,
        String vaccineCode,
        String vaccineDisplay,
        LocalDate administrationDate,
        String lotNumber,
        String site,
        Integer doseNumber,
        String encounterId
) {}