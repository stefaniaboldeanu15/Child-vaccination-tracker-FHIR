package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDateTime;
import java.util.List;

public record AdverseEventDTO(
        String adverseEventId,
        String patientId,
        String encounterId,
        String immunizationId,
        String category,          // e.g. product-problem | adverse-effect
        String severity,          // mild | moderate | severe
        String outcome,           // resolved | ongoing | fatal
        LocalDateTime date,
        String code,              // e.g. SNOMED / MedDRA code
        String display,           // human-readable description
        String description,       // free text
        List<String> suspectImmunizationIds
) {
}

