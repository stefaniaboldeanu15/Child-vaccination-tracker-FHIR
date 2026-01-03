package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Practitioner;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.PractitionerDTO;
import org.springframework.stereotype.Service;

@Service
public class PractitionerService {

    private final IGenericClient fhirClient;

    public PractitionerService(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    public PractitionerDTO createPractitioner(CreatePractitionerRequestDTO request) {

        Practitioner practitioner = new Practitioner();

        // --- Identifier (business identifier) ---
        practitioner.addIdentifier()
                .setSystem("http://example.org/practitioner-identifier")
                .setValue(request.getPractitionerIdentifier());

        // --- Name ---
        HumanName name = new HumanName();
        name.addGiven(request.getFirstName());
        name.setFamily(request.getLastName());
        practitioner.addName(name);

        // --- Create on FHIR server ---
        MethodOutcome outcome = fhirClient.create()
                .resource(practitioner)
                .execute();

        Practitioner created = (Practitioner) outcome.getResource();
        return toPractitionerDTO(created);
    }

    /**
     * Maps FHIR Practitioner → PractitionerDTO
     */
    private PractitionerDTO toPractitionerDTO(Practitioner practitioner) {

        PractitionerDTO dto = new PractitionerDTO();

        // FHIR technical id
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
        if (practitioner.hasName()) {
            HumanName name = practitioner.getNameFirstRep();

            if (name.hasGiven()) {
                if (!name.getGiven().isEmpty()) { // instead of (R4 version) dto.setFirstName(name.getGivenFirstRep().getValue());
                    dto.setFirstName(name.getGiven().get(0).getValue());
                }
            }
            dto.setLastName(name.getFamily());
        }

        return dto;
    }
}
