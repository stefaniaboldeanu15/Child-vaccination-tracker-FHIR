package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson;

import java.util.List;

/**
 * One row in the Related Person Dashboard patient list.
 * Combines Patient details + Related Person (parent / guardian).
 */
public record PatientDashboardRowDTO(
        PatientDetailsDTO patient,
        List<RelatedPersonDTO> relatedPersons
) {
}

