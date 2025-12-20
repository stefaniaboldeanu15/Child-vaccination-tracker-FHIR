package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class RelatedPersonDTO {

    private String relatedPersonId;
    private String relatedPersonIdentifier; // e.g. national ID, guardian ID
    private String relationship;            // "mother", "father", "guardian"
    private String fullName;
    private String phone;
    private String email;
    private String address;

    public RelatedPersonDTO() {
    }

    public RelatedPersonDTO(String relatedPersonId,
                            String relatedPersonIdentifier,
                            String relationship,
                            String fullName,
                            String phone,
                            String email,
                            String address) {
        this.relatedPersonId = relatedPersonId;
        this.relatedPersonIdentifier = relatedPersonIdentifier;
        this.relationship = relationship;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
    }

    // --- Getters & Setters ---

    public String getRelatedPersonId() {
        return relatedPersonId;
    }

    public void setRelatedPersonId(String relatedPersonId) {
        this.relatedPersonId = relatedPersonId;
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

    // Optional: helpful for logging / debugging
    @Override
    public String toString() {
        return "RelatedPersonDTO{" +
                "relatedPersonId='" + relatedPersonId + '\'' +
                ", relatedPersonIdentifier='" + relatedPersonIdentifier + '\'' +
                ", relationship='" + relationship + '\'' +
                ", fullName='" + fullName + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", address='" + address + '\'' +
                '}';
    }
}
