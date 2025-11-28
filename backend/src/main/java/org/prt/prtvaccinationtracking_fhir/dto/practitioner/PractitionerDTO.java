package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

/**
 * DTO used to display basic Practitioner information.
 */
public class PractitionerDTO {

    private String practitionerId;         /// FHIR Practitioner.id (technical id)
    private String practitionerIdentifier; /// Business identifier (e.g. CNP / SSN / license)

    private String firstName;
    private String lastName;

    /// --- getters & setters ---

    public String getPractitionerId() {
        return practitionerId;
    }

    public void setPractitionerId(String practitionerId) {
        this.practitionerId = practitionerId;
    }

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
}
