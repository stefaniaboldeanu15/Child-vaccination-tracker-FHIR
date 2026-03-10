package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal;

import java.time.LocalDate;

public record CreateGoalRequestDTO(
        String patientId,           // required in your app
        String lifecycleStatus,     // required (FHIR 1..1)
        String description,         // required (FHIR 1..1)
        LocalDate targetDueDate,
        LocalDate startDate,
        String carePlanId
) {}