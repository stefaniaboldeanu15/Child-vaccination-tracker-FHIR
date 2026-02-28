package org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization;

import java.time.LocalDate;

public record ImmunizationDTO(
        String id,
        String patientId,
        String vaccineCode,
        String vaccineDisplay,
        LocalDate administrationDate,
        Integer doseNumber,
        String lotNumber,
        String site,
        ImmunizationStatusDTO status,
        String practitionerName,
        String encounterId,
        boolean hasAdverseEvent
) {}