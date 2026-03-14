package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDateTime;

public record AppointmentDTO(
        String id,                  // FHIR Appointment.id
        String patientId,
        String patientName,
        String relatedPersonId,
        String relatedPersonName,
        LocalDateTime start,
        LocalDateTime end,
        String status,              // proposed, booked, arrived, fulfilled, cancelled, etc.
        String location,            // optional text for location
        String reason               // optional reason/description
) {
}

