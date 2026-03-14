package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.organization;

public record CreateOrganizationRequestDTO(
        String name,
        String type,      // hospital, clinic, private practice
        String address,
        String phone,
        String email
) {}
