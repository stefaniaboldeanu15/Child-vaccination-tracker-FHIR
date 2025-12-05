package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

/**
 * DTO used to display basic Organization information
 * ( hospital / clinic).
 */
public class OrganizationDTO {

    private String organizationId;
    private String name;


    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


}