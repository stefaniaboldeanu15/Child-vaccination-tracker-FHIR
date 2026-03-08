package org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization;

public record UpdateOrganizationRequestDTO(
        String name,
        String address,
        String phone,
        String email
) {}
