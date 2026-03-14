package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.organization;

public record UpdateOrganizationRequestDTO(
        String name,
        String address,
        String phone,
        String email
) {}
