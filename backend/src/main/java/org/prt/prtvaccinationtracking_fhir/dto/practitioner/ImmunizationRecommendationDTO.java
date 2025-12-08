package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.time.LocalDate;

public class ImmunizationRecommendationDTO {

    private String id;              // FHIR ImmunizationRecommendation.id
    private String patientId;
    private String vaccineCode;
    private String vaccineDisplay;
    private LocalDate dueDate;
    private String status;          // e.g. due, overdue, completed
    private String series;          // optional series name, e.g. "HepB series"
    private Integer doseNumber;     // optional

    public ImmunizationRecommendationDTO() {
    }

    public ImmunizationRecommendationDTO(String id,
                                         String patientId,
                                         String vaccineCode,
                                         String vaccineDisplay,
                                         LocalDate dueDate,
                                         String status,
                                         String series,
                                         Integer doseNumber) {
        this.id = id;
        this.patientId = patientId;
        this.vaccineCode = vaccineCode;
        this.vaccineDisplay = vaccineDisplay;
        this.dueDate = dueDate;
        this.status = status;
        this.series = series;
        this.doseNumber = doseNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
}
