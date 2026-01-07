package org.prt.prtvaccinationtracking_fhir.dto.practitioner;

import java.util.List;

/**
 * One row in the Practitioner Dashboard patient list.
 * Combines Patient details + Related Person (parent / guardian).
 */
public class PatientDashboardRowDTO {

    private PatientDetailsDTO patient; // Patient
    private List<RelatedPersonDTO> relatedPersons; // Related person (parent / guardian) - "list" because the child could have more than one related person

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
}
