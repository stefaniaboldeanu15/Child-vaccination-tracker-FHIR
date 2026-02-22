package org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication;

import java.time.LocalDateTime;

public record CommunicationDTO(
        String id,
        String patientId,
        String relatedPersonId,
        String recommendationId,
        String medium,
        String message,
        String status,
        LocalDateTime sentDate,
        LocalDateTime receivedDate
) {
}
