package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Practitioner;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.PractitionerDTO;
import org.springframework.stereotype.Component;

@Component
public class PractitionerMapper {

    public PractitionerDTO toDto(Practitioner practitioner) {

        PractitionerDTO dto = new PractitionerDTO();

        dto.setPractitionerId(
                practitioner.getIdElement().getIdPart()
        );

        if (practitioner.hasIdentifier()) {
            dto.setPractitionerIdentifier(
                    practitioner.getIdentifierFirstRep().getValue()
            );
        }

        if (practitioner.hasName()) {
            HumanName name = practitioner.getNameFirstRep();

            if (!name.getGiven().isEmpty()) {
                dto.setFirstName(name.getGiven().get(0).getValue());
            }

            dto.setLastName(name.getFamily());
        }

        return dto;
    }
}

