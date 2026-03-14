package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public record AllergyIntoleranceDTO(
        String allergyId,          // Unique ID
        String patientId,          // Reference to Patient
        String clinicalStatus,     // active | inactive | resolved
        String verificationStatus, // confirmed | unconfirmed | refuted
        String code,               // Allergy code (SNOMED, ICD, or local)
        String display,            // Human readable name ("Peanut allergy")
        String criticality,        // low | high | unable-to-assess
        String severity,           // mild | moderate | severe
        String reaction,           // description of reaction (rash, anaphylaxis, etc.)
        String onsetDate,          // YYYY-MM-DD
        String recorder            // Related person who recorded it
) {
}

