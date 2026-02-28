package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to display an Observation related to an Immunization
 * (e.g. reaction, vital sign, lab result after vaccination).
 */
public class ObservationDTO {

    private String observationId;/// FHIR Observation.id (technical id)
    private String immunizationId;/// The related Immunization.id (to know which dose this belongs to)
    private String code;/// Observation code (e.g. SNOMED/Loinc code)
    private String display;/// Human-readable name (e.g. "Body temperature")
    private String value;/// Measured value as text
    private String unit;/// Unit for the value
    private String effectiveDateTime; /// When the observation was made

    public String getObservationId() {
        return observationId;
    }

    public void setObservationId(String observationId) {
        this.observationId = observationId;
    }

    public String getImmunizationId() {
        return immunizationId;
    }

    public void setImmunizationId(String immunizationId) {
        this.immunizationId = immunizationId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDisplay() {
        return display;
    }

    public void setDisplay(String display) {
        this.display = display;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public String getEffectiveDateTime() {
        return effectiveDateTime;
    }

    public void setEffectiveDateTime(String effectiveDateTime) {
        this.effectiveDateTime = effectiveDateTime;
    }
}
