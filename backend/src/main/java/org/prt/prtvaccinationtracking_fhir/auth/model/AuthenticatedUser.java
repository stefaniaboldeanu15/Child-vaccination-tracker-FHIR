package org.prt.prtvaccinationtracking_fhir.auth.model;

import java.util.List;

public record AuthenticatedUser(
        String subject,
        String username,
        UserRole role,
        String displayName,
        String fhirUser,
        String practitionerId,
        List<String> relatedPersonIds,
        List<String> patientIds
) {
}
