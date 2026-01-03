package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PractitionerDashboardMapper;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PractitionerDashboardService {

    private final IGenericClient fhirClient;
    private final PractitionerDashboardMapper mapper;

    public PractitionerDashboardService(
            IGenericClient fhirClient,
            PractitionerDashboardMapper mapper
    ) {
        this.fhirClient = fhirClient;
        this.mapper = mapper;
    }

    // CĂUTARE DUPĂ SVNR
    public List<PatientDashboardRowDTO> searchBySvnr(String svnr) {

        Practitioner practitioner = getCurrentPractitioner();
        String practitionerId = practitioner.getIdElement().getIdPart();

        Bundle patientBundle = fhirClient.search()
                .forResource(Patient.class)
                .where(
                        Patient.IDENTIFIER.exactly()
                                .systemAndCode("https://elga.gv.at/svnr", svnr)
                )
                .where(
                        Patient.GENERAL_PRACTITIONER
                                .hasId("Practitioner/" + practitionerId)
                )
                .returnBundle(Bundle.class)
                .execute();

        return buildDashboardRows(patientBundle);
    }

    private List<PatientDashboardRowDTO> buildDashboardRows(Bundle patientBundle) {

        List<PatientDashboardRowDTO> rows = new ArrayList<>();

        for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {

            Patient patient = (Patient) entry.getResource();
            String patientId = patient.getIdElement().getIdPart();

            Bundle rpBundle = fhirClient.search()
                    .forResource(RelatedPerson.class)
                    .where(
                            RelatedPerson.PATIENT
                                    .hasId("Patient/" + patientId)
                    )
                    .returnBundle(Bundle.class)
                    .execute();

            PatientDashboardRowDTO row = new PatientDashboardRowDTO();
            row.setPatient(mapper.toPatientDetails(patient));
            row.setRelatedPersons(
                    rpBundle.getEntry().stream()
                            .map(e -> mapper.toRelatedPerson(
                                    (RelatedPerson) e.getResource()))
                            .toList()
            );

            rows.add(row);
        }

        return rows;
    }

    // DASHBOARD: list patients + related person
    public List<PatientDashboardRowDTO> getDashboardRows() {

        List<PatientDashboardRowDTO> rows = new ArrayList<>();

        // 1) Resolve logged-in practitioner
        Practitioner practitioner = getCurrentPractitioner();
        String practitionerId = practitioner.getIdElement().getIdPart();

        // 2) Get ONLY patients assigned to this practitioner
        Bundle patientBundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.GENERAL_PRACTITIONER.hasId(
                        "Practitioner/" + practitionerId
                ))
                .returnBundle(Bundle.class)
                .execute();

        for (Bundle.BundleEntryComponent entry : patientBundle.getEntry()) {

            Patient patient = (Patient) entry.getResource();

            PatientDashboardRowDTO row = new PatientDashboardRowDTO();
            row.setPatient(mapper.toPatientDetails(patient));

            // 3) Get related person (guardian)
            Bundle rpBundle = fhirClient.search()
                    .forResource(RelatedPerson.class)
                    .where(RelatedPerson.PATIENT.hasId(
                            patient.getIdElement().getIdPart()
                    ))
                    .returnBundle(Bundle.class)
                    .execute();

            if (!rpBundle.getEntry().isEmpty()) {
                RelatedPerson rp =
                        (RelatedPerson) rpBundle.getEntryFirstRep().getResource();
                row.setRelatedPersons((List<RelatedPersonDTO>) mapper.toRelatedPerson(rp));
            }

            rows.add(row);
        }

        return rows;
    }

    // Retrieves the patients, based on practitioner
    private Practitioner getCurrentPractitioner() {

        String username = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER
                        .exactly()
                        .identifier(username))
                .returnBundle(Bundle.class)
                .execute();

        if (!bundle.hasEntry()) {
            throw new RuntimeException(
                    "No Practitioner found for identifier: " + username
            );
        }

        return (Practitioner) bundle.getEntryFirstRep().getResource();
    }

    // CREATE Patient
    public String createPatient(CreatePatientRequestDTO dto) {

        Patient patient = mapper.toPatient(dto);

        MethodOutcome outcome = fhirClient.create()
                .resource(patient)
                .execute();

        return outcome.getId().getIdPart();
    }

    // UPDATE Patient
    public void updatePatient(String patientId, UpdatePatientRequestDTO dto) {

        Patient patient = fhirClient.read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();

        mapper.updatePatient(patient, dto);

        fhirClient.update()
                .resource(patient)
                .execute();
    }

    // CREATE RelatedPerson
    public String createRelatedPerson(CreateRelatedPersonRequestDTO dto) {

        RelatedPerson rp = mapper.toRelatedPerson(dto);

        MethodOutcome outcome = fhirClient.create()
                .resource(rp)
                .execute();

        return outcome.getId().getIdPart();
    }

    // UPDATE RelatedPerson
    public void updateRelatedPerson(
            String relatedPersonId,
            UpdateRelatedPersonRequestDTO dto) {

        RelatedPerson rp = fhirClient.read()
                .resource(RelatedPerson.class)
                .withId(relatedPersonId)
                .execute();

        mapper.updateRelatedPerson(rp, dto);

        fhirClient.update()
                .resource(rp)
                .execute();
    }
}
