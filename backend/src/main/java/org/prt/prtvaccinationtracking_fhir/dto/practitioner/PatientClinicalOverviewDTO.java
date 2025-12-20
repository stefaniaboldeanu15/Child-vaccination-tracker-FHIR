package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.util.List;

/**
 * Full clinical overview for one patient on the practitioner dashboard.
 *
 * Contains:
 *  - basic patient details
 *  - list of encounters, each including:
 *      EncounterDTO
 *      LocationDTO
 *      OrganizationDTO
 *      List<ImmunizationBlockDTO> (immunization + practitioner + observations)
 *      List<ObservationDTO> (other observations)
 *  - list of allergy intolerances
 */

public class PatientClinicalOverviewDTO {

    private PatientDetailsDTO patient;
    private List<EncounterBlockDTO> encounters;
    private List<AllergyIntoleranceDTO> allergies;

    public PatientClinicalOverviewDTO() {}

    public PatientClinicalOverviewDTO(
            PatientDetailsDTO patient,
            List<EncounterBlockDTO> encounters,
            List<AllergyIntoleranceDTO> allergies
    ) {
        this.patient = patient;
        this.encounters = encounters;
        this.allergies = allergies;
    }

    // ------------------- Getters & Setters -------------------

    public PatientDetailsDTO getPatient() {
        return patient;
    }

    public void setPatient(PatientDetailsDTO patient) {
        this.patient = patient;
    }

    public List<EncounterBlockDTO> getEncounters() {
        return encounters;
    }

    public void setEncounters(List<EncounterBlockDTO> encounters) {
        this.encounters = encounters;
    }

    public List<AllergyIntoleranceDTO> getAllergies() {
        return allergies;
    }

    public void setAllergies(List<AllergyIntoleranceDTO> allergies) {
        this.allergies = allergies;
    }

    @Override
    public String toString() {
        return "PatientClinicalOverviewDTO{" +
                "patient=" + patient +
                ", encounters=" + encounters +
                ", allergies=" + allergies +
                '}';
    }
}
