package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter;

import java.time.LocalDateTime;

public record UpdateEncounterRequestDTO(
        LocalDateTime start,
        LocalDateTime end,
        String reason,
        String location,
        String status

) {}

