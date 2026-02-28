package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * DTO used to display basic RelatedPerson information.
 */
public class RelatedPersonAccountDTO {

    private String relatedPersonId;         /// FHIR RelatedPerson.id (technical id)
    private String relatedPersonIdentifier; /// Business identifier (e.g. CNP / SSN / license)

    private String firstName;
    private String lastName;

    /// --- getters & setters ---

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
