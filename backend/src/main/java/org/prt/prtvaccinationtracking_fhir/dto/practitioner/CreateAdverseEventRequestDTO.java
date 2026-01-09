package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.time.LocalDateTime;
import java.util.List;

public class CreateAdverseEventRequestDTO {

    private String encounterId;
    private String immunizationId;

    private String category;     // adverse-effect | product-problem
    private String severity;     // mild | moderate | severe
    private String outcome;      // resolved | ongoing

    private String code;         // SNOMED / MedDRA code
    private String display;
    private String description;

    private LocalDateTime date;

    // Getters & setters
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

    public LocalDateTime getDate() {
        return date;
    }

    public void setDate(LocalDateTime date) {
        this.date = date;
    }
}
