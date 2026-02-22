package org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adeverseEvent.AdverseEventDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.OrganizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner.PractitionerDTO;

import java.util.List;

public record ImmunizationBlockDTO(
        ImmunizationDTO immunization,
        PractitionerDTO practitioner,
        OrganizationDTO organization,
        LocationDTO location,
        List<ObservationDTO> observations,
        List<AdverseEventDTO> adverseEvents
) {
}
