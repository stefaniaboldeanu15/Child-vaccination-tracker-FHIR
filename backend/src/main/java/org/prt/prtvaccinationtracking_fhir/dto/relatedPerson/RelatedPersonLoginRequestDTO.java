package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

public class RelatedPersonLoginRequestDTO {

    private String username;
    private String password;

    public RelatedPersonLoginRequestDTO() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
