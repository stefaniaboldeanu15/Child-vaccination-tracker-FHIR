package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.adverseEvent;
import org.hl7.fhir.r5.model.AdverseEvent;

import java.time.LocalDate;

public record AdverseEventDTO(

        String id,
        AdverseEvent.AdverseEventStatus status,
        AdverseEvent.AdverseEventActuality actuality,
        String category,
        LocalDate recordedDate,
        String encounter
) {
    public enum AdverseEventStatus {
        IN_PROGRESS,
        COMPLETED,
        ENTERED_IN_ERROR,
        UNKNOWN
    }
    public enum AdverseEventActuality {
        ACTUAL,
        POTENTIAL
    }

}