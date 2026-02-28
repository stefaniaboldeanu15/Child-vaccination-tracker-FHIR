package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

/**
 * Data Transfer Object for the API response after a successful login attempt.
 * Contains the generated access token and the high-level details of the authenticated related person.
 */
public class LoginResponseDTO {

    private String accessToken;
    private RelatedPersonAccountDTO relatedPerson;

    // Default Constructor (required for JSON deserialization)
    public LoginResponseDTO() {
    }

    /**
     * Full Constructor
     * @param accessToken The generated JWT or session token.
     * @param relatedPerson The header details of the logged-in related person.
     */
    public LoginResponseDTO(String accessToken, RelatedPersonAccountDTO relatedPerson) {
        this.accessToken = accessToken;
        this.relatedPerson = relatedPerson;
    }

    // --- Getters and Setters ---

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public RelatedPersonAccountDTO getRelatedPerson() {
        return relatedPerson;
    }

    public void setRelatedPerson(RelatedPersonAccountDTO relatedPerson) {
        this.relatedPerson = relatedPerson;
    }
}
