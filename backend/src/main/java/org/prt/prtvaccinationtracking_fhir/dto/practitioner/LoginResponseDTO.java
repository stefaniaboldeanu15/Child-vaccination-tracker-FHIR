package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

/**
 * Data Transfer Object for the API response after a successful login attempt.
 * Contains the generated access token and the high-level details of the authenticated practitioner.
 */
public class LoginResponseDTO {

    private String accessToken;
    private PractitionerHeaderDTO practitioner;

    // Default Constructor (required for JSON deserialization)
    public LoginResponseDTO() {
    }

    /**
     * Full Constructor
     * @param accessToken The generated JWT or session token.
     * @param practitioner The header details of the logged-in practitioner.
     */
    public LoginResponseDTO(String accessToken, PractitionerHeaderDTO practitioner) {
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

    public PractitionerHeaderDTO getPractitioner() {
        return practitioner;
    }

    public void setPractitioner(PractitionerHeaderDTO practitioner) {
        this.practitioner = practitioner;
    }
}