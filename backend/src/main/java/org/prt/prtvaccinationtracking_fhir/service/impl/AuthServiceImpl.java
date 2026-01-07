package org.prt.prtvaccinationtracking_fhir.service.impl;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.LoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.LoginResponseDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.RegistrationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PractitionerMapper;
import org.prt.prtvaccinationtracking_fhir.service.AuthService;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final IGenericClient fhirClient;
    private final PractitionerMapper practitionerMapper;

    public AuthServiceImpl(IGenericClient fhirClient, PractitionerMapper practitionerMapper) {
        this.fhirClient = fhirClient;
        this.practitionerMapper = practitionerMapper;
    }

    @Override
    public LoginResponseDTO authenticate(LoginRequestDTO request) {
        // 1️⃣ Caută Practitioner după identifier
        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER
                        .exactly()
                        .systemAndCode(
                                "http://hospital.smarthealthit.org/practitioners",
                                request.getIdentifier()))
                .returnBundle(Bundle.class)
                .execute();

        if (!bundle.hasEntry()) {
            throw new RuntimeException("Invalid identifier or password");
        }

        Practitioner practitioner =
                (Practitioner) bundle.getEntryFirstRep().getResource();

        // Verificare parolă (DEMO – din extension)
        String storedPassword = null;

        for (Extension ext : practitioner.getExtension()) {
            if ("http://example.org/extensions/password".equals(ext.getUrl())) {
                storedPassword = ((StringType) ext.getValue()).getValue();
            }
        }

        if (storedPassword == null || !storedPassword.equals(request.getPassword())) {
            throw new RuntimeException("Invalid identifier or password");
        }

        //  Construiește response
        LoginResponseDTO response = new LoginResponseDTO();
        response.setAccessToken("dummy-token"); // pentru frontend
        response.setPractitioner(
                practitionerMapper.toPractitioner(practitioner)
        );
        return response;
    }

    @Override
    public void register(RegistrationRequestDTO request) {
        if (request.getIdentifier() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Identifier and password are required");
        }
        // 1. Check if practitioner already exists
        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(request.getIdentifier()))
                .returnBundle(Bundle.class)
                .execute();
        if (bundle.hasEntry()) {
            throw new IllegalArgumentException("Practitioner with this identifier already exists");
        }

        // 2. Create Practitioner resource
        Practitioner practitioner = new Practitioner();
        practitioner.addIdentifier()
                .setSystem("http://hospital.smarthealthit.org/practitioners")
                .setValue(request.getIdentifier());
        // Name
        if (request.getFirstName() != null || request.getLastName() != null) {
            HumanName name = practitioner.addName();
            if (request.getFirstName() != null) {
                name.addGiven(request.getFirstName());
            }
            if (request.getLastName() != null) {
                name.setFamily(request.getLastName());
            }
        }
        // Optional: store password in a custom extension (for demo)
        if (request.getPassword() != null) {
            practitioner.addExtension()
                    .setUrl("http://example.org/extensions/password")
                    .setValue(new StringType(request.getPassword()));
        }

        // 3. Save to FHIR server
        MethodOutcome outcome = fhirClient.create()
                .resource(practitioner)
                .execute();
        System.out.println("Registered practitioner with ID: " + outcome.getId().getIdPart());
    }
}