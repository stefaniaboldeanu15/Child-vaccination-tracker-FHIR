package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal;

public record UpdateGoalRequestDTO(
        String patientId,           // required in your app
        String lifecycleStatus,     // required (FHIR 1..1)
        String description       // required (FHIR 1..1)

) {

}
