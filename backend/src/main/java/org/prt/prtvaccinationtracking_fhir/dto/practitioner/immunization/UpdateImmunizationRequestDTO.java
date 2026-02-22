package org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization;

public record UpdateImmunizationRequestDTO(

        String lotNumber,
        String site,
        Status status,
        String notes

) {

    public enum Status {
        completed,
        entered_in_error
    }
}