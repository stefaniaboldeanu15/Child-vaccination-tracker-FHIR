package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.time.LocalDateTime;

public class AppointmentDTO {

    private String id;                  // FHIR Appointment.id
    private String patientId;
    private String patientName;
    private String relatedPersonId;
    private String relatedPersonName;
    private LocalDateTime start;
    private LocalDateTime end;
    private String status;              // proposed, booked, arrived, fulfilled, cancelled, etc.
    private String location;            // optional text for location
    private String reason;              // optional reason/description

    public AppointmentDTO() {
    }

    public AppointmentDTO(String id,
                          String patientId,
                          String patientName,
                          String relatedPersonId,
                          String relatedPersonName,
                          LocalDateTime start,
                          LocalDateTime end,
                          String status,
                          String location,
                          String reason) {
        this.id = id;
        this.patientId = patientId;
        this.patientName = patientName;
        this.relatedPersonId = relatedPersonId;
        this.relatedPersonName = relatedPersonName;
        this.start = start;
        this.end = end;
        this.status = status;
        this.location = location;
        this.reason = reason;
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

    public String getPatientName() {
        return patientName;
    }

    public void setPatientName(String patientName) {
        this.patientName = patientName;
    }

    public String getRelatedPersonId() {
        return relatedPersonId;
    }

    public void setRelatedPersonId(String relatedPersonId) {
        this.relatedPersonId = relatedPersonId;
    }

    public String getRelatedPersonName() {
        return relatedPersonName;
    }

    public void setRelatedPersonName(String relatedPersonName) {
        this.relatedPersonName = relatedPersonName;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
