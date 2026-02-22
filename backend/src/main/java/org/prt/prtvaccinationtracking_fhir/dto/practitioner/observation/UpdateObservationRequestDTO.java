package org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation;

public record UpdateObservationRequestDTO(
        String value,
        String unit
) {
}
