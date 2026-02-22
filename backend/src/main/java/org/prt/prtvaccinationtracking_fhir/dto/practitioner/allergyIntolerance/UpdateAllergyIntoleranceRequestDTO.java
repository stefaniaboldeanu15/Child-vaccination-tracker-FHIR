package org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance;

public record UpdateAllergyIntoleranceRequestDTO(

        String severity,
        String reactionDescription,
        String status

) {}