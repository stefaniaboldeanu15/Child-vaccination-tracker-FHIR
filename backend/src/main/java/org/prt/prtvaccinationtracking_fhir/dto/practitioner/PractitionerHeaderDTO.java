package org.prt.prtvaccinationtracking_fhir.dto.practitioner;
/**
 * Data Transfer Object for returning key practitioner details after successful login.
 */
public class PractitionerHeaderDTO {

    private Long id;
    private String fullName;
    private String organizationName;

    // Default Constructor
    public PractitionerHeaderDTO() {
    }

    // Full Constructor
    public PractitionerHeaderDTO(Long id, String fullName, String organizationName) {
        this.id = id;
        this.fullName = fullName;
        this.organizationName = organizationName;
    }

    // --- Getters and Setters ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getOrganizationName() {
        return organizationName;
    }

    public void setOrganizationName(String organizationName) {
        this.organizationName = organizationName;
    }
}