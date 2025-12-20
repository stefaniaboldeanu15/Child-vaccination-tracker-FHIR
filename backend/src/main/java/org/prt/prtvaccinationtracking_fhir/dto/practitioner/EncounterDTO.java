package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class EncounterDTO {

    /// --- Technical IDs ---
    private String encounterId;   // FHIR Encounter id
    private String patientId;     // FHIR Patient id

    /// --- Main info you show in UI ---
    private String status;        // planned | in-progress | finished |

  //private String reasonCode;    // optional: first reason code
    private String reasonDisplay; // e.g. "Immunization visit"

    private String startDateTime; // encounter.period.start as String
    private String endDateTime;


    /// --- getters and setters ---
    /// Encounter ID
    public String getEncounterId() {
        return encounterId;
    }
    public void setEncounterId (String encounterId) {
        this.encounterId = encounterId;
    }

    /// Patient ID
    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    /// Encounter status
    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    /// Encounter reason

    /** public String getReasonCode() {
        return reasonCode;
    }

    public void setReasonCode(String reasonCode) {
        this.reasonCode = reasonCode;
    }

    public String getReasonDisplay() {
        return reasonDisplay;
    }

    public void setReasonDisplay(String reasonDisplay) {
        this.reasonDisplay = reasonDisplay;
    }
*/

    /// Encounter date
    public String getStartDateTime() {
        return startDateTime;
    }

    public void setStartDateTime(String startDateTime) {
        this.startDateTime = startDateTime;
    }

    public String getEndDateTime() {
        return endDateTime;
    }

    public void setEndDateTime(String endDateTime) {
        this.endDateTime = endDateTime;
    }

}
