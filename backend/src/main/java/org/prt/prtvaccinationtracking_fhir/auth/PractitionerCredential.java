package org.prt.prtvaccinationtracking_fhir.auth;

/**
 * Simple in-memory credential model for a practitioner.
 *
 * This class is used by {@link CredentialStore} to deserialize entries from
 * <code>practitioner-credentials.json</code> using Jackson. Each instance
 * represents one practitioner's login credentials (identifier + password).
 */

public class PractitionerCredential {

    private String identifier; ///Unique login identifier for the practitioner
    private String password;  ///Plain-text password used ONLY for development

    public PractitionerCredential() {
    }

    public PractitionerCredential(String identifier, String password) {
        this.identifier = identifier;
        this.password = password;
    }

    public String getIdentifier() {
        return identifier; /// Returns the practitioner's unique identifier
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }
    /**
     * Sets the practitioner's password.
     * Called by Jackson when mapping the JSON field "password".
     */

    public String getPassword() {   // <--- AuthServiceImpl calls this
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
