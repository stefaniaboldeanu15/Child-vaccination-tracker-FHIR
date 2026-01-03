package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

public class UpdatePatientRequestDTO {
    private String patientIdentifier; // SSN
    private String firstName;
    private String lastName;
    private String birthDate; // yyyy-MM-dd
    private String gender;

    // getters & setters
    public UpdatePatientRequestDTO() {
    }
    public UpdatePatientRequestDTO(String patientIdentifier,
                                   String firstName,
                                   String lastName,
                                   String birthDate,
                                   String gender) {
        this.patientIdentifier = patientIdentifier;
        this.firstName = firstName;
        this.lastName = lastName;
        this.birthDate = birthDate;
        this.gender = gender;
    }

    public String getPatientId() {
        return patientIdentifier;
    }

    public void setPatientId(String patientIdentifier) {
        this.patientIdentifier = patientIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) { this.firstName = firstName; }

    public  String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) { this.lastName = lastName; }

    public  String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) { this.birthDate = birthDate; }

    public  String getGender() {
        return gender;
    }

    public void setGender(String gender) { this.gender = gender; }

    // Optional: helpful for logging / debugging
    @Override
    public String toString() {
        return "CreatePatientRequestDTO{" +
                "patientIdentifier='" + patientIdentifier + '\'' +
                "firstName='" + firstName + '\'' +
                "lastName='" + lastName + '\'' +
                "birthDate='" + birthDate + '\'' +
                "gender='" + gender + '\'' +
                '}';
    }

}
