package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

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
public record EncounterBlockDTO(
        EncounterDTO encounter,
        LocationDTO location,                       /// from Immunization.location
        OrganizationDTO organization,               /// from Immunization.performer
        List<ImmunizationBlockDTO> immunizations,
        List<ObservationDTO> observations           /// all observations in this encounter
) {
}

