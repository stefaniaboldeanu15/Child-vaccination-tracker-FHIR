package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class AllergyIntoleranceDTO {

    private String allergyId;          // Unique ID
    private String patientId;          // Reference to Patient

    private String clinicalStatus;     // active | inactive | resolved
    private String verificationStatus; // confirmed | unconfirmed | refuted

    private String code;               // Allergy code (SNOMED, ICD, or local)
    private String display;            // Human readable name (“Peanut allergy”)

    private String criticality;        // low | high | unable-to-assess
    private String severity;           // mild | moderate | severe

    private String reaction;           // description of reaction (rash, anaphylaxis, etc.)
    private String onsetDate;          // YYYY-MM-DD
    private String recorder;           // Practitioner who recorded it

    public AllergyIntoleranceDTO() {}

    public AllergyIntoleranceDTO(
            String allergyId,
            String patientId,
            String clinicalStatus,
            String verificationStatus,
            String code,
            String display,
            String criticality,
            String severity,
            String reaction,
            String onsetDate,
            String recorder
    ) {
        this.allergyId = allergyId;
        this.patientId = patientId;
        this.clinicalStatus = clinicalStatus;
        this.verificationStatus = verificationStatus;
        this.code = code;
        this.display = display;
        this.criticality = criticality;
        this.severity = severity;
        this.reaction = reaction;
        this.onsetDate = onsetDate;
        this.recorder = recorder;
    }

    public String getAllergyId() {
        return allergyId;
    }

    public void setAllergyId(String allergyId) {
        this.allergyId = allergyId;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public String getClinicalStatus() {
        return clinicalStatus;
    }

    public void setClinicalStatus(String clinicalStatus) {
        this.clinicalStatus = clinicalStatus;
    }

    public String getVerificationStatus() {
        return verificationStatus;
    }

    public void setVerificationStatus(String verificationStatus) {
        this.verificationStatus = verificationStatus;
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

    public String getCriticality() {
        return criticality;
    }

    public void setCriticality(String criticality) {
        this.criticality = criticality;
    }

    public String getSeverity() {
        return severity;
    }

    public void setSeverity(String severity) {
        this.severity = severity;
    }

    public String getReaction() {
        return reaction;
    }

    public void setReaction(String reaction) {
        this.reaction = reaction;
    }

    public String getOnsetDate() {
        return onsetDate;
    }

    public void setOnsetDate(String onsetDate) {
        this.onsetDate = onsetDate;
    }

    public String getRecorder() {
        return recorder;
    }

    public void setRecorder(String recorder) {
        this.recorder = recorder;
    }
}
