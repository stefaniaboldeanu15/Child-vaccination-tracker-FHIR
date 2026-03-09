package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.util.List;

/**
 * DTO representing one Immunization "block" in the Patient Clinical Overview.
 *
 * Contains:
 *  - the immunization itself
 *  - the practitioner who performed it
 *  - observations related to this immunization
 */
public record ImmunizationBlockDTO(
        ImmunizationDTO immunization,
        PractitionerDTO practitioner,
        List<ObservationDTO> observations
) {
}

