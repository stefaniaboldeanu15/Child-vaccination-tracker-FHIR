package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.PractitionerDTO;
import org.springframework.stereotype.Component;

@Component
public class PractitionerMapper {

    // -------------------------------------------------
    // DTO → FHIR (CREATE)
    // -------------------------------------------------
    public Practitioner createPractitioner(CreatePractitionerRequestDTO dto) {

        Practitioner practitioner = new Practitioner();

        // Identifier (username / business identifier)
        practitioner.addIdentifier()
                .setSystem("http://hospital.smarthealthit.org/practitioners")
                .setValue(dto.getPractitionerIdentifier());

        // Name
        HumanName name = new HumanName();
        name.setFamily(dto.getLastName());
        name.addGiven(dto.getFirstName());
        practitioner.addName(name);

        // Password extension (mock / demo only)
        if (dto.getPassword() != null && !dto.getPassword().isBlank()) {
            Extension passwordExt = new Extension();
            passwordExt.setUrl("http://example.org/extensions/password");
            passwordExt.setValue(new StringType(dto.getPassword()));
            practitioner.addExtension(passwordExt);
        }

        return practitioner;
    }

    // -------------------------------------------------
    // FHIR → DTO (READ)
    // -------------------------------------------------
    public PractitionerDTO toPractitioner(Practitioner practitioner) {

        PractitionerDTO dto = new PractitionerDTO();

        // FHIR technical ID
        dto.setPractitionerId(
                practitioner.getIdElement().getIdPart()
        );

        // Business identifier
        if (practitioner.hasIdentifier()) {
            dto.setPractitionerIdentifier(
                    practitioner.getIdentifierFirstRep().getValue()
            );
        }

        // Name
        if (practitioner.hasName() && !practitioner.getName().isEmpty()) {
            HumanName name = practitioner.getNameFirstRep();
            dto.setFirstName(name.getGivenAsSingleString());
            dto.setLastName(name.getFamily());
        }

        return dto;
    }
}
