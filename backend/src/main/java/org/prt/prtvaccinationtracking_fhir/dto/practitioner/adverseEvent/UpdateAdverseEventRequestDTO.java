package org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent;

import java.time.LocalDate;

public record UpdateAdverseEventRequestDTO(

        AdverseEventDTO.AdverseEventStatus status,
        AdverseEventDTO.AdverseEventActuality actuality,
        String category,
        LocalDate recordedDate,
        String encounter

) {}
