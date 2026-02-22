package org.prt.prtvaccinationtracking_fhir.dto.practitioner.adeverseEvent;
import java.time.LocalDate;

public record AdverseEventDTO(

        String id,
        String patientId,
        String immunizationId,
        String description,
        String severity,
        LocalDate onsetDate,
        String outcome,
        String reportedBy,     // practitioner name
        LocalDate recordedDate

) {}