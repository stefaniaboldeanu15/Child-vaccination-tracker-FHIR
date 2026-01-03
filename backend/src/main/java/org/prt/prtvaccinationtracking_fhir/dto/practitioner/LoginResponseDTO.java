package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

/**
 * Data Transfer Object for the API response after a successful login attempt.
 * Contains the generated access token and the high-level details of the authenticated practitioner.
 */
public class LoginResponseDTO {

    private String accessToken;
    private PractitionerDTO practitioner;

    // Default Constructor (required for JSON deserialization)
    public LoginResponseDTO() {
    }

    /**
     * Full Constructor
     * @param accessToken The generated JWT or session token.
     * @param practitioner The header details of the logged-in practitioner.
     */
    public LoginResponseDTO(String accessToken, PractitionerDTO practitioner) {
        this.accessToken = accessToken;
        this.practitioner = practitioner;
    }

    // --- Getters and Setters ---

    public String getAccessToken() {
        return accessToken;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public PractitionerDTO getPractitioner() {
        return practitioner;
    }

    public void setPractitioner(PractitionerDTO practitioner) {
        this.practitioner = practitioner;
    }
}