package org.prt.prtvaccinationtracking_fhir.dto.practitioner;
/**
 * Data Transfer Object for the API request during login (identifier and password).
 */
public class LoginRequestDTO {

    private String identifier; // SSN or other practitioner identifier
    private String password;

    // Default Constructor
    public LoginRequestDTO() {
    }

    // --- Getters and Setters ---
    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}