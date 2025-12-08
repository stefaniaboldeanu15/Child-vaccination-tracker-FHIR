package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.time.LocalDate;

public class CreateImmunizationRecommendationRequest {

    private String vaccineCode;     // required
    private String vaccineDisplay;  // optional
    private LocalDate dueDate;      // required
    private String series;          // optional
    private Integer doseNumber;     // optional
    private String notes;           // optional free text

    public CreateImmunizationRecommendationRequest() {
    }

    public CreateImmunizationRecommendationRequest(String vaccineCode,
                                                   String vaccineDisplay,
                                                   LocalDate dueDate,
                                                   String series,
                                                   Integer doseNumber,
                                                   String notes) {
        this.vaccineCode = vaccineCode;
        this.vaccineDisplay = vaccineDisplay;
        this.dueDate = dueDate;
        this.series = series;
        this.doseNumber = doseNumber;
        this.notes = notes;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public Integer getDoseNumber() {
        return doseNumber;
    }

    public void setDoseNumber(Integer doseNumber) {
        this.doseNumber = doseNumber;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
