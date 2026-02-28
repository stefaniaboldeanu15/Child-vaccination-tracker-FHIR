package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to create a new RelatedPerson (account).
 */
public class CreateRelatedPersonAccountRequestDTO {

    /**
     * Business identifier (e.g. username, license number)
     * Will be stored as RelatedPerson.identifier.value
     */
    private String relatedPersonIdentifier;

    private String firstName;
    private String lastName;
    private String password;

    // --- getters & setters ---

    public String getRelatedPersonIdentifier() {
        return relatedPersonIdentifier;
    }

    public void setRelatedPersonIdentifier(String relatedPersonIdentifier) {
        this.relatedPersonIdentifier = relatedPersonIdentifier;
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
