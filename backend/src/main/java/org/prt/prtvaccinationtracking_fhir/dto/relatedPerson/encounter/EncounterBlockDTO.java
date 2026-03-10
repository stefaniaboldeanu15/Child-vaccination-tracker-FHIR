package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.ImmunizationBlockDTO;

import java.util.List;

public record EncounterBlockDTO(

        EncounterDTO encounter,
        List<ImmunizationBlockDTO> immunizations

) {}