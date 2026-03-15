package org.prt.prtvaccinationtracking_fhir.auth.dto;

import java.util.List;

public record AuthSessionResponse(
        String username,
        String role,
        String displayName,
        String fhirUser,
        String practitionerId,
        List<String> relatedPersonIds,
        List<String> patientIds
) {
}
