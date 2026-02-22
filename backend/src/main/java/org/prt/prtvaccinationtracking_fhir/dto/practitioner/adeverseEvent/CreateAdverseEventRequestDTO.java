package org.prt.prtvaccinationtracking_fhir.dto.practitioner.adeverseEvent;
import java.time.LocalDate;
public record  CreateAdverseEventRequestDTO (

    String immunizationId,      // linked to a specific vaccine
    String description,         // what happened
    Severity severity,
    LocalDate onsetDate,
    String outcome,             // recovered / ongoing / hospitalized / fatal
    String notes
) {

    public enum Severity {
        mild,
        moderate,
        severe
    }
}
