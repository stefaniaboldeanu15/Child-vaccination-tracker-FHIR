package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.util.List;

/**
 * DTO representing one Encounter block in the Patient Clinical Overview.
 *
 * Contains:
 *  - EncounterDTO          : main encounter information
 *  - LocationDTO           : where the encounter happened
 *  - OrganizationDTO       : hospital/clinic
 *  - List<ImmunizationBlockDTO> : immunizations that happened in this encounter
 */
public class EncounterBlockDTO {

    private EncounterDTO encounter;
    private LocationDTO location;                       /// from Immunization.location
    private OrganizationDTO organization;               /// from Immunization.performer
    private List<ImmunizationBlockDTO> immunizations;
    private List<ObservationDTO> observations;          /// all observations in this encounter

    public EncounterDTO getEncounter() {
        return encounter;
    }

    public void setEncounter(EncounterDTO encounter) {
        this.encounter = encounter;
    }

    public LocationDTO getLocation() {
        return location;
    }

    public void setLocation(LocationDTO location) {
        this.location = location;
    }

    public OrganizationDTO getOrganization() {
        return organization;
    }

    public void setOrganization(OrganizationDTO organization) {
        this.organization = organization;
    }

    public List<ImmunizationBlockDTO> getImmunizations() {
        return immunizations;
    }

    public void setImmunizations(List<ImmunizationBlockDTO> immunizations) {
        this.immunizations = immunizations;
    }

    public List<ObservationDTO> getObservations() {
        return observations;
    }

    public void setObservations(List<ObservationDTO> observations) {
        this.observations = observations;
    }

    // ---- toString override (useful for logging/debugging) ----
    @Override
    public String toString() {
        return "EncounterBlockDTO{" +
                "encounter=" + encounter +
                ", location=" + location +
                ", organization=" + organization +
                ", immunizations=" + immunizations +
                ", observations=" + observations +
                '}';
    }
}
