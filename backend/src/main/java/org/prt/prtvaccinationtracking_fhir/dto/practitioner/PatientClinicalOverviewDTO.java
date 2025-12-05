package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.util.List;

/**
 * Full clinical overview for one patient (the child) on the practitioner dashboard.
 * Contains:
 *  - basic patient details
 *  - parents/guardians (RelatedPerson)
 *  - list of encounters, each with location, organization, immunizations, observations
 */
/**
 * Clinical history grouped by encounters.
 * Each EncounterBlockDTO already contains:
 *  - EncounterDTO
 *  - LocationDTO (organization's location)
 *  - OrganizationDTO
 *  - List<ImmunizationBlockDTO> (Immunization + Practitioner + Obs)
 *  - List<ObservationDTO> (other observations for the encounter)
 */
public class PatientClinicalOverviewDTO {

    private PatientDetailsDTO patient;
    private List<RelatedPersonDTO> relatedPersons;
    private List<EncounterBlockDTO> encounters;

    public PatientClinicalOverviewDTO() {
    }

    public PatientClinicalOverviewDTO(PatientDetailsDTO patient,
                                      List<RelatedPersonDTO> relatedPersons,
                                      List<EncounterBlockDTO> encounters) {
        this.patient = patient;
        this.relatedPersons = relatedPersons;
        this.encounters = encounters;
    }

    // ---- Getters & Setters ----

    public PatientDetailsDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDetailsDTO patient) {
        this.patient = patient;
    }

    public List<RelatedPersonDTO> getRelatedPersons() {
        return relatedPersons;
    }

    public void setRelatedPersons(List<RelatedPersonDTO> relatedPersons) {
        this.relatedPersons = relatedPersons;
    }

    public List<EncounterBlockDTO> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<EncounterBlockDTO> encounters) {
        this.encounters = encounters;
    }

    @Override
    public String toString() {
        return "PatientClinicalOverviewDTO{" +
                "patient=" + patient +
                ", relatedPersons=" + relatedPersons +
                ", encounters=" + encounters +
                '}';
    }
}
