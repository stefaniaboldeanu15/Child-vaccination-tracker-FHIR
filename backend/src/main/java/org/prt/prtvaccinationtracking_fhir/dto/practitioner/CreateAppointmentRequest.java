package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.time.LocalDateTime;

public class CreateAppointmentRequest {

    private String patientId;       // required
    private LocalDateTime start;    // required
    private LocalDateTime end;      // optional (could be calculated)
    private String location;        // optional
    private String reason;          // optional description

    public CreateAppointmentRequest() {
    }

    public CreateAppointmentRequest(String patientId,
                                    LocalDateTime start,
                                    LocalDateTime end,
                                    String location,
                                    String reason) {
        this.patientId = patientId;
        this.start = start;
        this.end = end;
        this.location = location;
        this.reason = reason;
    }

    public String getPatientId() {
        return patientId;
    }

    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getEnd() {
        return end;
    }

    public void setEnd(LocalDateTime end) {
        this.end = end;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
