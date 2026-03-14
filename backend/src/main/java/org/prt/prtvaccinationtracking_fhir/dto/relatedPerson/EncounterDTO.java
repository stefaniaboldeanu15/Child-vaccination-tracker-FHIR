package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record EncounterDTO(
        String encounterId,   // FHIR Encounter id
        String patientId,     // FHIR Patient id
        String status,        // planned | in-progress | finished |
        String reasonDisplay, // e.g. "Immunization visit"
        String startDateTime, // encounter.period.start as String
        String endDateTime
) {
}

