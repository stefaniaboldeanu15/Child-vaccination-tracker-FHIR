package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication;

public record UpdateCommunicationRequestDTO(
        String status   // sent, delivered, read, failed
) {
}