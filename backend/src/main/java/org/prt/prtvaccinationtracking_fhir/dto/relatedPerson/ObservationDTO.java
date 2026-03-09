package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to display an Observation related to an Immunization
 * (e.g. reaction, vital sign, lab result after vaccination).
 */
public record ObservationDTO(
        String observationId,    /// FHIR Observation.id (technical id)
        String immunizationId,   /// The related Immunization.id (to know which dose this belongs to)
        String code,             /// Observation code (e.g. SNOMED/Loinc code)
        String display,          /// Human-readable name (e.g. "Body temperature")
        String value,            /// Measured value as text
        String unit,             /// Unit for the value
        String effectiveDateTime /// When the observation was made
) {
}

