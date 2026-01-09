package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class UpdateRelatedPersonRequestDTO {

    private String relationship;
    private String fullName;
    private String phone;
    private String email;
    private String address;

    // getters & setters
    public UpdateRelatedPersonRequestDTO () {
    }
    public UpdateRelatedPersonRequestDTO ( String relationship,
                                           String fullName,
                                           String phone,
                                           String email,
                                           String address) {
        this.relationship = relationship;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;

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
