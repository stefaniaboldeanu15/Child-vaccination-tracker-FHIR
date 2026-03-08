package org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation;

import java.time.LocalDateTime;

public record ObservationDTO(
        String id,
        String code,
        String display,
        String value,
        String unit,
        LocalDateTime effectiveDateTime
) {
}
