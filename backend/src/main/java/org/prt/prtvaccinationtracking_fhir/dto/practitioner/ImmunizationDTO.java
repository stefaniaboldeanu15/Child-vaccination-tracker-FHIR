package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class ImmunizationDTO {

    private String immunizationId; ///FHIR Immunization.id (technical id)
    private String patientId; ///FHIR Patient.id this immunization belongs to
    private String encounterId; /// FHIR Encounter.id in which it was given
    private String organizationId; ///FHIR Organization.id (hospital/clinic that provided it)
    private String vaccineCode; /// Code of the vaccine (e.g. CVX)
    private String vaccineDisplay; /// Human-readable vaccine name
    private String occurrenceDateTime; /// When the dose was given (occurrenceDateTime)
    private String status; /// completed | entered-in-error | not-done | etc.

    public ImmunizationDTO() {
    }

    public ImmunizationDTO(String immunizationId,
                           String patientId,
                           String encounterId,
                           String organizationId,
                           String vaccineCode,
                           String vaccineDisplay,
                           String occurrenceDateTime,
                           String status) {
        this.immunizationId = immunizationId;
        this.patientId = patientId;
        this.encounterId = encounterId;
        this.organizationId = organizationId;
        this.vaccineCode = vaccineCode;
        this.vaccineDisplay = vaccineDisplay;
        this.occurrenceDateTime = occurrenceDateTime;
        this.status = status;
    }

    public String getImmunizationId() {
        return immunizationId;
    }

    public void setImmunizationId(String immunizationId) {
        this.immunizationId = immunizationId;
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

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
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

    public String getOccurrenceDateTime() {
        return occurrenceDateTime;
    }

    public void setOccurrenceDateTime(String occurrenceDateTime) {
        this.occurrenceDateTime = occurrenceDateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "ImmunizationDTO{" +
                "immunizationId='" + immunizationId + '\'' +
                ", patientId='" + patientId + '\'' +
                ", encounterId='" + encounterId + '\'' +
                ", organizationId='" + organizationId + '\'' +
                ", vaccineCode='" + vaccineCode + '\'' +
                ", vaccineDisplay='" + vaccineDisplay + '\'' +
                ", occurrenceDateTime='" + occurrenceDateTime + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
