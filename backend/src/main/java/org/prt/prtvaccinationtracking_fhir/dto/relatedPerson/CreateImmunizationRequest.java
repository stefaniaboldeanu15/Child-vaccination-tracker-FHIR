package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDate;

public record CreateImmunizationRequest(
        String vaccineCode,     // required
        String vaccineDisplay,  // optional label
        LocalDate date,         // required
        String immunizationId,  // optional
        String performerId      // optional: FHIR Practitioner.id if you want to set it
) {
}

