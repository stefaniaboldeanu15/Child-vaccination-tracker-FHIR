package org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal;

import java.time.LocalDate;
import java.util.List;

public record GoalDTO(
        String id,
        String patientId,

        // lifecycleStatus (1..1) - use the FHIR codes
        String lifecycleStatus, // proposed|planned|accepted|active|on-hold|completed|cancelled|entered-in-error|rejected

        // FHIR R5: description (1..1 CodeableConcept) -> we store as text
        String description,

        // FHIR R5: target.dueDate (date) - optional
        LocalDate targetDueDate,

        // Optional but useful for your planning UI (FHIR R5: startDate)
        LocalDate startDate,

        // OPTIONAL app-link (not native): you may keep it as you did (identifier)
        String carePlanId
) {}