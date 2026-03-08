package org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal;

import java.time.LocalDate;

public record UpdateGoalRequestDTO(
        String patientId,           // required in your app
        String lifecycleStatus,     // required (FHIR 1..1)
        String description       // required (FHIR 1..1)

) {

}
