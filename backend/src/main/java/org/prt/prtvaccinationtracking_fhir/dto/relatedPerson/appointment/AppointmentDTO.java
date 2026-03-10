package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment;

import java.time.LocalDateTime;

public record AppointmentDTO(
        String id,
        String status,
        LocalDateTime start,
        LocalDateTime end,
        String reason,
        String practitionerName,
        String patientId,
        String locationId
) {
}
