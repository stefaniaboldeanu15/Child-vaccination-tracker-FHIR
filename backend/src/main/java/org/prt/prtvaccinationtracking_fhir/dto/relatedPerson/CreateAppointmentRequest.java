package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDateTime;

public record CreateAppointmentRequest(
        String patientId,       // required
        LocalDateTime start,    // required
        LocalDateTime end,      // optional (could be calculated)
        String location,        // optional
        String reason           // optional description
) {
}

