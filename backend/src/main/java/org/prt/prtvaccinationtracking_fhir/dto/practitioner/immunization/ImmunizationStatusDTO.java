package org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization;

public enum ImmunizationStatusDTO {
    completed("completed"),
    entered_in_error("entered-in-error"),
    not_done("not-done");

    private final String fhirCode;

    ImmunizationStatusDTO(String fhirCode) {
        this.fhirCode = fhirCode;
    }

    public String toFhirCode() {
        return fhirCode;
    }

    public static ImmunizationStatusDTO fromFhirCode(String code) {
        if (code == null) return null;
        for (ImmunizationStatusDTO s : values()) {
            if (s.fhirCode.equals(code)) return s;
        }
        return null;
    }
}