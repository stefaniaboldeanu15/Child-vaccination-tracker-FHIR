package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record ImmunizationDTO(
        String immunizationId,      /// FHIR Immunization.id (technical id)
        String patientId,           /// FHIR Patient.id this immunization belongs to
        String encounterId,         /// FHIR Encounter.id in which it was given
        String organizationId,      /// FHIR Organization.id (hospital/clinic that provided it)
        String vaccineCode,         /// Code of the vaccine (e.g. CVX)
        String vaccineDisplay,      /// Human-readable vaccine name
        String occurrenceDateTime,  /// When the dose was given (occurrenceDateTime)
        String status               /// completed | entered-in-error | not-done | etc.
) {
}

