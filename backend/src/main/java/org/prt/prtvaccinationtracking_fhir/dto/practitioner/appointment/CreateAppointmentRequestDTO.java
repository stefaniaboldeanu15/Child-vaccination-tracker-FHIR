package org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment;

import java.time.LocalDateTime;

public record CreateAppointmentRequestDTO(
        String patientId,
        LocalDateTime start,
        LocalDateTime end,
        String reason,
        String locationId
) {
}
