package org.prt.prtvaccinationtracking_fhir.service.impl;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonLoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonLoginResponseDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonRegistrationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;

@Service
public class RelatedPersonAuthServiceImpl implements RelatedPersonAuthService {

    private static final String PATIENT_ID_SYSTEM =
            "http://example.org/patient-portal/username";

    private static final String PASSWORD_EXT_URL =
            "http://example.org/extensions/patient-password";

    private final IGenericClient fhirClient;

    public RelatedPersonAuthServiceImpl(IGenericClient fhirClient) {
        this.fhirClient = fhirClient;
    }

    // =========================
    // REGISTER
    // =========================
    private void validateUnder18(LocalDate birthDate) {
        if (birthDate == null) {
            throw new IllegalArgumentException("Birth date is required");
        }

        int age = Period.between(birthDate, LocalDate.now()).getYears();
        if (age >= 18) {
            throw new IllegalArgumentException(
                    "Registration allowed only for persons under 18 years old"
            );
        }
    }

    @Override
    public void register(RelatedPersonRegistrationRequestDTO request) {

        // Parse birth date
        LocalDate birthDate = null;
        if (request.getBirthDate() != null && !request.getBirthDate().isBlank()) {
            birthDate = LocalDate.parse(request.getBirthDate());
        }

        // Validate age
        validateUnder18(birthDate);

        // Prevent duplicate usernames
        Bundle existing = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly()
                        .systemAndCode(PATIENT_ID_SYSTEM, request.getUsername()))
                .returnBundle(Bundle.class)
                .execute();

        if (existing.hasEntry()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Create Patient
        Patient patient = new Patient();

        patient.addIdentifier()
                .setSystem(PATIENT_ID_SYSTEM)
                .setValue(request.getUsername());

        // Name
        if (request.getFirstName() != null || request.getLastName() != null) {
            HumanName name = patient.addName();
            if (request.getFirstName() != null) {
                name.addGiven(request.getFirstName());
            }
            if (request.getLastName() != null) {
                name.setFamily(request.getLastName());
            }
        }

        // Birth date
        patient.setBirthDate(
                java.util.Date.from(
                        birthDate.atStartOfDay(ZoneId.systemDefault()).toInstant()
                )
        );

        // Password extension
        patient.addExtension()
                .setUrl(PASSWORD_EXT_URL)
                .setValue(new StringType(request.getPassword()));

        // Save
        MethodOutcome outcome = fhirClient.create()
                .resource(patient)
                .execute();

        System.out.println("Registered patient with ID: "
                + outcome.getId().getIdPart());
    }

    // =========================
    // LOGIN
    // =========================
    @Override
    public RelatedPersonLoginResponseDTO login(RelatedPersonLoginRequestDTO request) {

        // Search by username
        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.IDENTIFIER.exactly()
                        .systemAndCode(PATIENT_ID_SYSTEM, request.getUsername()))
                .returnBundle(Bundle.class)
                .execute();

        if (!bundle.hasEntry()) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Find the correct Patient
        Patient patient = null;

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Patient p = (Patient) entry.getResource();
            Extension pwExt = p.getExtensionByUrl(PASSWORD_EXT_URL);
            if (pwExt != null && pwExt.getValue() instanceof StringType) {
                patient = p;
                break;
            }
        }

        if (patient == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Validate password
        String storedPassword = patient
                .getExtensionByUrl(PASSWORD_EXT_URL)
                .getValue()
                .primitiveValue();

        if (!storedPassword.equals(request.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        // Build response
        RelatedPersonLoginResponseDTO resp =
                new RelatedPersonLoginResponseDTO();

        resp.setAccessToken("dummy-token");
        resp.setPatientId(patient.getIdElement().getIdPart());

        if (patient.hasName()) {
            HumanName n = patient.getNameFirstRep();
            if (n.hasGiven()) {
                resp.setFirstName(n.getGivenAsSingleString());
            }
            if (n.hasFamily()) {
                resp.setLastName(n.getFamily());
            }
        }

        return resp;
    }
}
