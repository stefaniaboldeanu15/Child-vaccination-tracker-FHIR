package org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication;

public record UpdateCommunicationRequestDTO(
        String status   // sent, delivered, read, failed
) {
}
