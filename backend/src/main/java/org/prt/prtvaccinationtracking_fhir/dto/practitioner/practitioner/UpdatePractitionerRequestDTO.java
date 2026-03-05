package org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner;

public record UpdatePractitionerRequestDTO(
        String firstName,
        String lastName,
        String specialization,
        String phone,
        String email,
        String organizationId

) {}