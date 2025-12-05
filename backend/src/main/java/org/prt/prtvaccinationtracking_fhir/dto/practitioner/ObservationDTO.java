package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

/**
 * DTO used to display an Observation related to an Immunization
 * (e.g. reaction, vital sign, lab result after vaccination).
 * Later, in your ObservationMapper, you’ll map from FHIR Observation (R5) to this DTO by:
 * observationId ← obs.getIdElement().getIdPart()
 * immunizationId from Observation.basedOn / focus reference (where it points to Immunization)
 * code / display from Observation.code.codingFirstRep()
 * value / unit from Observation.valueQuantity or valueString
 * effectiveDateTime from Observation.effectiveDateTimeType (or other effective[x]).
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