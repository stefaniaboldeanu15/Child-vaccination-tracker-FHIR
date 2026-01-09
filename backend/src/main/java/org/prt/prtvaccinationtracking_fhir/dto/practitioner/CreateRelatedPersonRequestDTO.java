package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class CreateRelatedPersonRequestDTO {

    private String patientId;              // CHILD Patient.id
    private String relatedPersonIdentifier;
    private String relationship;            // mother / father / guardian
    private String fullName;
    private String phone;
    private String email;
    private String address;

    // getters & setters
    public CreateRelatedPersonRequestDTO() {

    }
    public CreateRelatedPersonRequestDTO(String patientId,
                                         String relatedPersonIdentifier,
                                         String relationship,
                                         String fullName,
                                         String phone,
                                         String email,
                                         String address) {
        this.patientId = patientId;
        this.relatedPersonIdentifier = relatedPersonIdentifier;
        this.relationship = relationship;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;

    }
    public String getPatientId() {
        return patientId;
    }
    public void setPatientId(String patientId) {
        this.patientId = patientId;
    }
    public String getRelatedPersonIdentifier() {
        return relatedPersonIdentifier;
    }
    public void setRelatedPersonIdentifier(String relatedPersonIdentifier) {
        this.relatedPersonIdentifier = relatedPersonIdentifier;
    }
    public String getRelationship() {
        return relationship;
    }
    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }
    public String getFullName() {
        return fullName;
    }
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }
    public String getEmail() {
        return email;
    }
    public void setEmail(String email) {
        this.email = email;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }

}
