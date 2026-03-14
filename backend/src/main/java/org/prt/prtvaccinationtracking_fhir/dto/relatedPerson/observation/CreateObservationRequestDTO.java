package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation;

import java.time.LocalDateTime;

public record CreateObservationRequestDTO(
        String code,
        String display,
        String value,
        String unit,
        LocalDateTime effectiveDateTime,
        String encounterId
) {
}
