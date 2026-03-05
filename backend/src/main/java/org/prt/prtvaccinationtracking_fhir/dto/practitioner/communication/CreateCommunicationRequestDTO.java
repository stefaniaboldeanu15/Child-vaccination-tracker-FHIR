package org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication;

import java.time.LocalDateTime;

public record CreateCommunicationRequestDTO(

        String patientId,
        String relatedPersonId,
        String recommendationId,
        String medium,      // sms, email, portal
        String message,
        LocalDateTime sentDate

) {}