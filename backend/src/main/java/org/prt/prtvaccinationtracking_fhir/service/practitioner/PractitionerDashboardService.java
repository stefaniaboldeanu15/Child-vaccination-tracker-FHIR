package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PractitionerDashboardMapper;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PractitionerMapper;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PractitionerPatientOverviewMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PractitionerDashboardService {

    private final IGenericClient fhirClient;
    private final PractitionerDashboardMapper mapper;
    private final PractitionerPatientOverviewMapper overviewMapper;
    private final PractitionerMapper  practitionerMapper;

    public PractitionerDashboardService(
            IGenericClient fhirClient,
            PractitionerDashboardMapper mapper,
            PractitionerPatientOverviewMapper overviewMapper,
            PractitionerMapper  practitionerMapper
    ) {
        this.fhirClient = fhirClient;
        this.mapper = mapper;
        this.overviewMapper = overviewMapper;
        this.practitionerMapper = practitionerMapper;
    }
    /// credentials of the current practitioner
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private Practitioner getCurrentPractitioner() {
        String username = getCurrentUsername();
        if (username == null) {
            throw new RuntimeException("No authenticated user found in security context.");
        }

        Bundle bundle = fhirClient.search()
                .forResource(Practitioner.class)
                .where(Practitioner.IDENTIFIER.exactly().identifier(username))
                .returnBundle(Bundle.class)
                .execute();

        if (!bundle.hasEntry() || bundle.getEntry().isEmpty()) {
            throw new RuntimeException("No Practitioner found with identifier = " + username);
        }

        return (Practitioner) bundle.getEntryFirstRep().getResource();
    }

/**
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
    }*/

    // ─────────────────────────────────────────────────────────
    // PRACTITIONER DASHBOARD
    // ─────────────────────────────────────────────────────────

    /// search by SVNR
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

    /// builds the row of the searched patient by SVNR
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

    /// DASHBOARD: list patients + related person (COMPLETE LIST -ALL PATIENTS)
    public List<PatientDashboardRowDTO> getMyPatients() {

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
                row.setRelatedPersons(
                        rpBundle.getEntry().stream()
                                .map(e -> (RelatedPerson) e.getResource())
                                .map(mapper::toRelatedPerson)
                                .toList()
                );
            }

            rows.add(row);
        }

        return rows;
    }

    /// CREATE Patient
    public String createPatient(CreatePatientRequestDTO dto) {

        Patient patient = mapper.toPatient(dto);

        MethodOutcome outcome = fhirClient.create()
                .resource(patient)
                .execute();

        return outcome.getId().getIdPart();
    }

    /// UPDATE Patient
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

    /// CREATE RelatedPerson
    public String createRelatedPerson(CreateRelatedPersonRequestDTO dto) {

        RelatedPerson rp = mapper.toRelatedPerson(dto);

        MethodOutcome outcome = fhirClient.create()
                .resource(rp)
                .execute();

        return outcome.getId().getIdPart();
    }

    /// UPDATE RelatedPerson
    public void updateRelatedPerson(String relatedPersonId, UpdateRelatedPersonRequestDTO dto) {

        RelatedPerson rp = fhirClient.read()
                .resource(RelatedPerson.class)
                .withId(relatedPersonId)
                .execute();

        mapper.updateRelatedPerson(rp, dto);

        fhirClient.update()
                .resource(rp)
                .execute();
    }

    // ─────────────────────────────────────────────────────────
    // PRACTITIONER - PATIENT OVERVIEW (encounters + immunizations + obs + allergies)
    // ─────────────────────────────────────────────────────────
    public PatientClinicalOverviewDTO getPatientClinicalOverview(String patientId) {

        PatientClinicalOverviewDTO overview = new PatientClinicalOverviewDTO();

        // 1) Patient
        Patient patient = fhirClient.read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();
        overview.setPatient(mapper.toPatientDetails(patient));

        // 3) Encounters for this patient
        List<EncounterBlockDTO> encounterBlocks = new ArrayList<>();
        Bundle encBundle = fhirClient.search()
                .forResource(Encounter.class)
                .where(Encounter.SUBJECT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        for (Bundle.BundleEntryComponent encEntry : encBundle.getEntry()) {

            Encounter encounter = (Encounter) encEntry.getResource();
            EncounterBlockDTO block = new EncounterBlockDTO();

            // 3a) Encounter core data
            block.setEncounter(overviewMapper.toEncounter(encounter));

            String encId = encounter.getIdElement().getIdPart();

            // 3b) Organization (serviceProvider)
            if (encounter.hasServiceProvider()
                    && encounter.getServiceProvider().getReferenceElement().hasIdPart()) {

                String orgId = encounter.getServiceProvider()
                        .getReferenceElement()
                        .getIdPart();

                Organization org = fhirClient.read()
                        .resource(Organization.class)
                        .withId(orgId)
                        .execute();
                block.setOrganization(overviewMapper.toOrganization(org));
            }

            // 3c) Location (first Encounter.location) + its managing organization
            if (encounter.hasLocation() && !encounter.getLocation().isEmpty()) {
                Reference locRef = encounter.getLocationFirstRep().getLocation();
                if (locRef != null && locRef.getReferenceElement().hasIdPart()) {
                    String locId = locRef.getReferenceElement().getIdPart();

                    Location loc = fhirClient.read()
                            .resource(Location.class)
                            .withId(locId)
                            .execute();

                    Organization managingOrg = null;
                    if (loc.hasManagingOrganization()
                            && loc.getManagingOrganization().getReferenceElement().hasIdPart()) {
                        String locOrgId = loc.getManagingOrganization()
                                .getReferenceElement()
                                .getIdPart();

                        managingOrg = fhirClient.read()
                                .resource(Organization.class)
                                .withId(locOrgId)
                                .execute();
                    }

                    block.setLocation(overviewMapper.toLocation(loc, managingOrg));
                }
            }

            // 3d) Immunizations for this encounter
            List<ImmunizationBlockDTO> immBlocks = new ArrayList<>();

            // The server does NOT support search parameter "encounter" for Immunization,
            // so we search by patient and filter by encounter id in Java.
            Bundle immBundle = fhirClient.search()
                    .forResource(Immunization.class)
                    .where(Immunization.PATIENT.hasId(patientId))
                    .returnBundle(Bundle.class)
                    .execute();

            for (Bundle.BundleEntryComponent immEntry : immBundle.getEntry()) {
                Immunization imm = (Immunization) immEntry.getResource();

                // Keep only immunizations that belong to this encounter
                if (!imm.hasEncounter()
                        || !imm.getEncounter().getReferenceElement().hasIdPart()
                        || !encId.equals(imm.getEncounter().getReferenceElement().getIdPart())) {
                    continue;
                }

                ImmunizationBlockDTO immBlock = new ImmunizationBlockDTO();
                immBlock.setImmunization(overviewMapper.toImmunization(imm));

                // Practitioner who performed it
                PractitionerDTO pracDto = null;
                if (imm.hasPerformer() && !imm.getPerformer().isEmpty()) {
                    Reference actorRef = imm.getPerformerFirstRep().getActor();
                    if (actorRef != null && actorRef.getReferenceElement().hasIdPart()) {
                        String pracId = actorRef.getReferenceElement().getIdPart();
                        Practitioner practitioner = fhirClient.read()
                                .resource(Practitioner.class)
                                .withId(pracId)
                                .execute();
                        pracDto = practitionerMapper.toPractitioner(practitioner);
                    }
                }
                immBlock.setPractitioner(pracDto);

                immBlocks.add(immBlock);
            }
            block.setImmunizations(immBlocks);

            // 3e) Observations for this encounter
            List<ObservationDTO> obsDtos = new ArrayList<>();
            Bundle obsBundle = fhirClient.search()
                    .forResource(Observation.class)
                    .where(Observation.ENCOUNTER.hasId(encId))
                    .returnBundle(Bundle.class)
                    .execute();

            for (Bundle.BundleEntryComponent obsEntry : obsBundle.getEntry()) {
                Observation obs = (Observation) obsEntry.getResource();
                obsDtos.add(overviewMapper.toObservation(obs, null));
            }
            block.setObservations(obsDtos);

            encounterBlocks.add(block);
        }

        overview.setEncounters(encounterBlocks);
        return overview;
    }

    public PatientDetailsDTO getPatientDetails(String patientId) {
        Patient patient = fhirClient.read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();

        return mapper.toPatientDetails(patient);
    }

    public List<EncounterBlockDTO> getEncounterBlocksForPatient(String patientId) {

        List<EncounterBlockDTO> encounterBlocks = new ArrayList<>();

        Bundle encBundle = fhirClient.search()
                .forResource(Encounter.class)
                .where(Encounter.SUBJECT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        for (Bundle.BundleEntryComponent encEntry : encBundle.getEntry()) {

            Encounter encounter = (Encounter) encEntry.getResource();
            EncounterBlockDTO block = new EncounterBlockDTO();
            String encId = encounter.getIdElement().getIdPart();

            // —— Encounter core data ——
            block.setEncounter(overviewMapper.toEncounter(encounter));

            // —— Organization (serviceProvider) ——
            if (encounter.hasServiceProvider() &&
                    encounter.getServiceProvider().getReferenceElement().hasIdPart()) {

                String orgId = encounter.getServiceProvider().getReferenceElement().getIdPart();

                Organization org = fhirClient.read()
                        .resource(Organization.class)
                        .withId(orgId)
                        .execute();

                block.setOrganization(overviewMapper.toOrganization(org));
            }

            // —— Location + managing organization ——
            if (encounter.hasLocation() && !encounter.getLocation().isEmpty()) {

                Reference locRef = encounter.getLocationFirstRep().getLocation();

                if (locRef.getReferenceElement().hasIdPart()) {

                    String locId = locRef.getReferenceElement().getIdPart();

                    Location loc = fhirClient.read()
                            .resource(Location.class)
                            .withId(locId)
                            .execute();

                    Organization managingOrg = null;

                    if (loc.hasManagingOrganization() &&
                            loc.getManagingOrganization().getReferenceElement().hasIdPart()) {

                        String locOrgId = loc.getManagingOrganization()
                                .getReferenceElement()
                                .getIdPart();

                        managingOrg = fhirClient.read()
                                .resource(Organization.class)
                                .withId(locOrgId)
                                .execute();
                    }

                    block.setLocation(overviewMapper.toLocation(loc, managingOrg));
                }
            }

            // —— Immunizations belonging to this encounter ——
            List<ImmunizationBlockDTO> immBlocks = new ArrayList<>();

            Bundle immBundle = fhirClient.search()
                    .forResource(Immunization.class)
                    .where(Immunization.PATIENT.hasId(patientId))
                    .returnBundle(Bundle.class)
                    .execute();

            for (Bundle.BundleEntryComponent immEntry : immBundle.getEntry()) {

                Immunization imm = (Immunization) immEntry.getResource();

                if (!imm.hasEncounter() ||
                        !imm.getEncounter().getReferenceElement().hasIdPart() ||
                        !encId.equals(imm.getEncounter().getReferenceElement().getIdPart())) {
                    continue;
                }

                ImmunizationBlockDTO immBlock = new ImmunizationBlockDTO();
                immBlock.setImmunization(overviewMapper.toImmunization(imm));

                // Practitioner
                PractitionerDTO pracDto = null;
                if (imm.hasPerformer() && !imm.getPerformer().isEmpty()) {
                    Reference actorRef = imm.getPerformerFirstRep().getActor();
                    if (actorRef.getReferenceElement().hasIdPart()) {
                        String pracId = actorRef.getReferenceElement().getIdPart();
                        Practitioner practitioner = fhirClient.read()
                                .resource(Practitioner.class)
                                .withId(pracId)
                                .execute();
                        pracDto = practitionerMapper.toPractitioner(practitioner);
                    }
                }
                immBlock.setPractitioner(pracDto);

                immBlocks.add(immBlock);
            }

            block.setImmunizations(immBlocks);

            // —— Observations for this encounter ——
            List<ObservationDTO> obsDtos = new ArrayList<>();

            Bundle obsBundle = fhirClient.search()
                    .forResource(Observation.class)
                    .where(Observation.ENCOUNTER.hasId(encId))
                    .returnBundle(Bundle.class)
                    .execute();

            for (Bundle.BundleEntryComponent obsEntry : obsBundle.getEntry()) {
                Observation obs = (Observation) obsEntry.getResource();
                obsDtos.add(overviewMapper.toObservation(obs, null));
            }

            block.setObservations(obsDtos);

            encounterBlocks.add(block);
        }

        return encounterBlocks;
    }

    public List<AllergyIntoleranceDTO> getAllergiesForPatient(String patientId) {

        Bundle bundle = fhirClient.search()
                .forResource(AllergyIntolerance.class)
                .where(AllergyIntolerance.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<AllergyIntoleranceDTO> result = new ArrayList<>();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            AllergyIntolerance ai = (AllergyIntolerance) entry.getResource();
            result.add(overviewMapper.toAllergyIntolerance(ai));
        }

        return result;
    }

    /// GET Immunization for patient

    public List<ImmunizationDTO> getImmunizationsForPatient (String patientId) {
        Bundle immBundle = fhirClient.search()
                .forResource(Immunization.class)
                .where(Immunization.PATIENT.hasId("Patient/"+patientId))
                .returnBundle(Bundle.class)
                .execute();
        return immBundle.getEntry().stream()
                .map(e->(Immunization) e.getResource())
                .map(overviewMapper::toImmunization)
                .collect(Collectors.toList());
    }

    /// PUT - Create Immunization for patient
    public ImmunizationDTO createImmunizationForPatient(
            String patientId,
            CreateImmunizationRequest request){

        Immunization imm = new Immunization();

        imm.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);
        imm.setPatient(new Reference("Patient/"+ patientId));

        //Vaccine code
        CodeableConcept cc = new CodeableConcept();
        Coding coding = cc.addCoding();
        coding.setSystem("http://hl7.org/fhir/sid/cvx");
        coding.setCode(request.getVaccineCode());
        coding.setDisplay(request.getVaccineDisplay());
        imm.setVaccineCode(cc);

        //Date
        if(request.getDate() != null){
            imm.setOccurrence(new DateTimeType(java.util.Date.from(
                    request.getDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            )));
        }
        // Performer = logged-in practitioner
        Practitioner practitioner = getCurrentPractitioner();
        String pracId = practitioner.getIdElement().getIdPart();

        Immunization.ImmunizationPerformerComponent performer = new Immunization.ImmunizationPerformerComponent();
        performer.setActor(new Reference("Practitioner/" + pracId));
        imm.addPerformer(performer);

        MethodOutcome outcome;
        // CUSTOM IMMUNIZATION ID SUPPORT — PUT instead of POST
        if (request.getImmunizationId() != null && !request.getImmunizationId().isBlank()) {

            // Set custom ID
            imm.setId("Immunization/" + request.getImmunizationId());

            // PUT → update/create with specific ID
            outcome = fhirClient.update()
                    .resource(imm)
                    .execute();
        } else {
            // POST → let FHIR server generate ID
            outcome = fhirClient.create()
                    .resource(imm)
                    .execute();
        }

        Immunization created = (Immunization) outcome.getResource();
        return overviewMapper.toImmunization(created);
    }

    /// GET Immunization recommendation for patient
    public List<ImmunizationRecommendationDTO> getRecommendationsForPatient(String patientId) {
        Bundle recBundle = fhirClient.search()
                .forResource(ImmunizationRecommendation.class)
                .where(ImmunizationRecommendation.PATIENT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<ImmunizationRecommendationDTO> result = new ArrayList<>();

        for (Bundle.BundleEntryComponent entry : recBundle.getEntry()) {
            ImmunizationRecommendation rec = (ImmunizationRecommendation) entry.getResource();
            String recId = rec.getIdElement().getIdPart();

            for (ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent comp
                    : rec.getRecommendation()) {
                result.add(overviewMapper.toRecommendationDTO(recId, patientId, comp));
            }
        }

        return result;
    }

    /// PUT Immunization recommendation for patient
    public ImmunizationRecommendationDTO createRecommendationForPatient(
            String patientId,
            CreateImmunizationRecommendationRequest request
    ) {
        ImmunizationRecommendation rec = new ImmunizationRecommendation();
        rec.setPatient(new Reference("Patient/" + patientId));

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent comp =
                new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent();

        CodeableConcept vaccineCode = new CodeableConcept();
        Coding coding = vaccineCode.addCoding();
        coding.setSystem("http://example.org/vaccine-codes"); // TODO: use real terminology system
        coding.setCode(request.getVaccineCode());
        coding.setDisplay(request.getVaccineDisplay());
        comp.addVaccineCode(vaccineCode);

        // Due date as date criterion (simple model)
        if (request.getDueDate() != null) {
            ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent dc =
                    new ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent();
            dc.setCode(new CodeableConcept().setText("due-date"));
            dc.setValue(java.util.Date.from(
                    request.getDueDate().atStartOfDay(ZoneId.systemDefault()).toInstant()
            ));
            comp.addDateCriterion(dc);
        }

        if (request.getSeries() != null) {
            comp.setSeries(request.getSeries());
        }
        if (request.getDoseNumber() != null) {
            comp.setDoseNumber(String.valueOf(request.getDoseNumber()));
        }

        // Very simple forecast status
        if (request.getNotes() != null && !request.getNotes().isBlank()) {
            CodeableConcept status = new CodeableConcept();
            status.setText(request.getNotes());
            comp.setForecastStatus(status);
        }

        rec.addRecommendation(comp);

        MethodOutcome outcome = fhirClient.create()
                .resource(rec)
                .execute();

        ImmunizationRecommendation created = (ImmunizationRecommendation) outcome.getResource();
        String recId = created.getIdElement().getIdPart();
        return overviewMapper.toRecommendationDTO(recId, patientId, created.getRecommendationFirstRep());
    }
    public List<AppointmentDTO> getAppointmentsForPatient(String patientId) {
        Bundle bundle = fhirClient.search()
                .forResource(Appointment.class)
                .where(Appointment.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<AppointmentDTO> result = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Appointment appt = (Appointment) entry.getResource();
            result.add(overviewMapper.toAppointmentDTO(appt));
        }
        return result;
    }

    public AppointmentDTO createAppointment(CreateAppointmentRequest request) {
        Practitioner practitioner = getCurrentPractitioner();
        String practitionerId = practitioner.getIdElement().getIdPart();

        Appointment appt = new Appointment();
        appt.setStatus(Appointment.AppointmentStatus.BOOKED);

        if (request.getStart() != null) {
            appt.setStart(java.util.Date.from(
                    request.getStart().atZone(ZoneId.systemDefault()).toInstant()
            ));
        }
        if (request.getEnd() != null) {
            appt.setEnd(java.util.Date.from(
                    request.getEnd().atZone(ZoneId.systemDefault()).toInstant()
            ));
        }

        if (request.getReason() != null && !request.getReason().isBlank()) {
            CodeableConcept cc = new CodeableConcept();
            cc.setText(request.getReason());
            CodeableReference ref = new CodeableReference();
            ref.setConcept(cc);
            appt.addReason(ref);
        }

        if (request.getLocation() != null && !request.getLocation().isBlank()) {
            appt.setDescription(request.getLocation());
        }

        // Participant: patient
        Appointment.AppointmentParticipantComponent patientPart =
                new Appointment.AppointmentParticipantComponent();
        patientPart.setActor(new Reference("Patient/" + request.getPatientId()));
        appt.addParticipant(patientPart);

        // Participant: practitioner
        Appointment.AppointmentParticipantComponent pracPart =
                new Appointment.AppointmentParticipantComponent();
        pracPart.setActor(new Reference("Practitioner/" + practitionerId));
        appt.addParticipant(pracPart);

        MethodOutcome outcome = fhirClient.create()
                .resource(appt)
                .execute();

        Appointment created = (Appointment) outcome.getResource();
        return overviewMapper.toAppointmentDTO(created);
    }

    public AppointmentDTO updateAppointmentStatus(String appointmentId, String newStatusCode) {
        Appointment appt = fhirClient.read()
                .resource(Appointment.class)
                .withId(appointmentId)
                .execute();

        Appointment.AppointmentStatus newStatus =
                Appointment.AppointmentStatus.fromCode(newStatusCode);
        appt.setStatus(newStatus);

        MethodOutcome outcome = fhirClient.update()
                .resource(appt)
                .execute();

        Appointment updated = (Appointment) outcome.getResource();
        return overviewMapper.toAppointmentDTO(updated);
    }

    public void createFullEncounter(
            CreateFullEncounterRequest request,
            String patientId
    ) {
        Practitioner practitioner = getCurrentPractitioner();
        String practitionerId = practitioner.getIdElement().getIdPart();

        Encounter encounter = overviewMapper.toEncounter(
                request,
                patientId,
                practitionerId
        );

        MethodOutcome outcome = fhirClient.create()
                .resource(encounter)
                .execute();

        Encounter savedEncounter = (Encounter) outcome.getResource();
        String encounterId = savedEncounter.getIdElement().getIdPart();

        if (request.getImmunizations() != null) {
            for (CreateFullEncounterRequest.FullImmunizationInput immDto
                    : request.getImmunizations()) {

                Immunization imm = overviewMapper.toImmunization(
                        immDto,
                        patientId,
                        encounterId
                );

                fhirClient.create().resource(imm).execute();
            }
        }

        if (request.getObservations() != null) {
            for (CreateFullEncounterRequest.FullObservationInput obsDto
                    : request.getObservations()) {

                Observation obs = overviewMapper.toObservation(
                        obsDto,
                        patientId,
                        encounterId
                );

                fhirClient.create().resource(obs).execute();
            }
        }
    }

    //List  ALL ADVERSE EVENTS
    public List<AdverseEventDTO> getAdverseEventsForPatient(String patientId) {

        Bundle bundle = fhirClient.search()
                .forResource(AdverseEvent.class)
                .where(AdverseEvent.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<AdverseEventDTO> result = new ArrayList<>();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            AdverseEvent ae = (AdverseEvent) entry.getResource();
            result.add(overviewMapper.toAdverseEvent(ae));
        }

        return result;
    }

    //List  ALL ADVERSE EVENTS FOR A SPECIFIC ENCOUNTER
    public List<AdverseEventDTO> getAdverseEventsForEncounter(
            String patientId,
            String encounterId
    ) {
        Bundle bundle = fhirClient.search()
                .forResource(AdverseEvent.class)
                .where(AdverseEvent.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<AdverseEventDTO> result = new ArrayList<>();

        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            AdverseEvent ae = (AdverseEvent) entry.getResource();

            if (!ae.hasEncounter()
                    || !ae.getEncounter().getReferenceElement().hasIdPart()
                    || !encounterId.equals(
                    ae.getEncounter().getReferenceElement().getIdPart())) {
                continue;
            }

            result.add(overviewMapper.toAdverseEvent(ae));
        }

        return result;
    }

    public void createAdverseEvent(
            String patientId,
            CreateAdverseEventRequestDTO dto
    ) {
        if (patientId == null || patientId.isBlank()) {
            throw new IllegalArgumentException("patientId is required");
        }

        // Map DTO → FHIR AdverseEvent
        AdverseEvent adverseEvent = overviewMapper.toAdverseEvent(
                dto,
                patientId
        );

        // Persist AdverseEvent
        fhirClient.create()
                .resource(adverseEvent)
                .execute();
    }






}
