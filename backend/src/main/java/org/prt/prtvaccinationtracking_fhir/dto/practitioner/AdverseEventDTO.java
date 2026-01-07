package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.time.LocalDateTime;
import java.util.List;

public class AdverseEventDTO {

    private String adverseEventId;
    // Context
    private String patientId;
    private String encounterId;
    private String immunizationId;

    // Event details
    private String category;          // e.g. product-problem | adverse-effect
    private String severity;          // mild | moderate | severe
    private String outcome;           // resolved | ongoing | fatal
    private LocalDateTime date;

    // Code / description
    private String code;               // e.g. SNOMED / MedDRA code
    private String display;            // human-readable description
    private String description;        // free text

    // related to immunizations

    private List<String> suspectImmunizationIds;

    // Getters / Setters
    public String getAdverseEventId() {
        return adverseEventId;
    }

    public void setAdverseEventId(String adverseEventId) {
        this.adverseEventId = adverseEventId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getImmunizationId() {
        return immunizationId;
    }

    public void setImmunizationId(String immunizationId) {
        this.immunizationId = immunizationId;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getOutcome() {
        return outcome;
    }

    public void setOutcome(String outcome) {
        this.outcome = outcome;
    }

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getSuspectImmunizationIds() {
        return suspectImmunizationIds;
    }

    public void setSuspectImmunizationIds(List<String> suspectImmunizationIds) {
        this.suspectImmunizationIds = suspectImmunizationIds;
    }
}
