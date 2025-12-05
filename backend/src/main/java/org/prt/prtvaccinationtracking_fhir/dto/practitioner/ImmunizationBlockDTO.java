package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.util.List;

/**
 * DTO representing one Immunization "block" in the Patient Clinical Overview.
 *
 * Contains:
 *  - the immunization itself
 *  - the practitioner who performed it
 *  - observations related to this immunization
 */

public class ImmunizationBlockDTO {

    private ImmunizationDTO immunization;   // main immunization data
    private PractitionerDTO practitioner;   // who performed it
    private List<ObservationDTO> observations; // related observations

    // --- getters & setters ---

    public ImmunizationDTO getImmunization() {
        return immunization;
    }

    public void setImmunization(ImmunizationDTO immunization) {
        this.immunization = immunization;
    }

    public PractitionerDTO getPractitioner() {
        return practitioner;
    }

    public void setPractitioner(PractitionerDTO practitioner) {
        this.practitioner = practitioner;
    }

    public List<ObservationDTO> getObservations() {
        return observations;
    }

    public void setObservations(List<ObservationDTO> observations) {
        this.observations = observations;
    }
}
