package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.adverseEvent;

import java.time.LocalDate;

public record  CreateAdverseEventRequestDTO (

        AdverseEventDTO.AdverseEventStatus status,
        AdverseEventDTO.AdverseEventActuality actuality,
        String patientId,
        String category,
        LocalDate recordedDate,
        String encounter
) {


}
