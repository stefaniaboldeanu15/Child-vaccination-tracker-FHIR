package org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization;

public record OrganizationDTO(
        String id,
        String name,
        String type,      // hospital / clinic / private practice
        String phone,
        String address
) {
}
