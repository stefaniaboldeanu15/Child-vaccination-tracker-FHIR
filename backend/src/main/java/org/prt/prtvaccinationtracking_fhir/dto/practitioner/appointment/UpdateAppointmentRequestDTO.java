package org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment;

import java.time.LocalDateTime;

public record UpdateAppointmentRequestDTO(
        LocalDateTime start,
        LocalDateTime end,
        String status
) {
}
