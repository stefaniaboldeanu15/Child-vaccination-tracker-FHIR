package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDateTime;

public record CreateAdverseEventRequestDTO(
        String encounterId,
        String immunizationId,
        String category,     // adverse-effect | product-problem
        String severity,     // mild | moderate | severe
        String outcome,      // resolved | ongoing
        String code,         // SNOMED / MedDRA code
        String display,
        String description,
        LocalDateTime date
) {
}

