package org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance;

public record AllergyIntoleranceDTO(
        String id,
        AllergyClinicalStatus clinicalStatus,
        AllergyType type,
        AllergyIntoleranceCategory category,
        AllergyCriticality criticality,
        String code,
        String patientId,
        String encounterId
) {
    //Compile-time Java restriction
    public enum AllergyClinicalStatus {
        ACTIVE,
        INACTIVE,
        RESOLVED
    }
    public enum AllergyType {
        ALLERGY,
        INTOLERANCE
    }
    public enum AllergyIntoleranceCategory {
        FOOD,
        MEDICATION,
        ENVIRONMENT,
        BIOLOGIC
    }
    public enum AllergyCriticality {
        LOW,
        HIGH,
        UNABLE_TO_ASSESS

    }

}
