package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.time.LocalDate;

public class CreateImmunizationRequest {

    private String vaccineCode;     // required
    private String vaccineDisplay;  // optional label
    private LocalDate date;         // required
    private String immunizationId;       // optional
    private String performerId;     // optional: FHIR Practitioner.id if you want to set it

    public CreateImmunizationRequest() {
    }

    public CreateImmunizationRequest(String vaccineCode,
                                     String vaccineDisplay,
                                     LocalDate date,
                                     String immunizationId,
                                     String performerId) {
        this.vaccineCode = vaccineCode;
        this.vaccineDisplay = vaccineDisplay;
        this.date = date;
        this.immunizationId = immunizationId;
        this.performerId = performerId;
    }

    public String getVaccineCode() {
        return vaccineCode;
    }

    public void setVaccineCode(String vaccineCode) {
        this.vaccineCode = vaccineCode;
    }

    public String getVaccineDisplay() {
        return vaccineDisplay;
    }

    public void setVaccineDisplay(String vaccineDisplay) {
        this.vaccineDisplay = vaccineDisplay;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getImmunizationId() {
        return immunizationId;
    }

    public void setImmunizationId(String immunizationId) {
        this.immunizationId = immunizationId;
    }


    public String getPerformerId() {
        return performerId;
    }

    public void setPerformerId(String performerId) {
        this.performerId = performerId;
    }
}
