package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to display basic Organization information
 * ( hospital / clinic).
 */
public record OrganizationDTO(
        String organizationId,
        String name
) {
}

