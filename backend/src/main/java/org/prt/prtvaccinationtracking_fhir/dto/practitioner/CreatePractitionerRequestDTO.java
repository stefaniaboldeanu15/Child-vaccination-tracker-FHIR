package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

/**
 * DTO used to create a new Practitioner.
 */
public class CreatePractitionerRequestDTO {

    /**
     * Business identifier (e.g. username, license number)
     * Will be stored as Practitioner.identifier.value
     */
    private String practitionerIdentifier;

    private String firstName;
    private String lastName;
    private String password;

    // --- getters & setters ---

    public String getPractitionerIdentifier() {
        return practitionerIdentifier;
    }

    public void setPractitionerIdentifier(String practitionerIdentifier) {
        this.practitionerIdentifier = practitionerIdentifier;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
