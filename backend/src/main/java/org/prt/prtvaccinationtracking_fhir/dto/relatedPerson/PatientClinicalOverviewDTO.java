package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.util.List;

/**
 * Full clinical overview for one patient on the related person dashboard.
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
public record PatientClinicalOverviewDTO(
        PatientDetailsDTO patient,
        List<EncounterBlockDTO> encounters,
        List<AllergyIntoleranceDTO> allergies
) {
}

