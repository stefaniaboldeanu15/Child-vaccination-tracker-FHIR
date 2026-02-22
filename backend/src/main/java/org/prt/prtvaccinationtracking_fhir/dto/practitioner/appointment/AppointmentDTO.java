package org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment;

import java.time.LocalDateTime;

public record AppointmentDTO(
        String id,
        String patientId,
        LocalDateTime start,
        LocalDateTime end,
        String status,          // booked, cancelled, fulfilled
        String reason,
        String locationName,
        String practitionerName
) {
}
