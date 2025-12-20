package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.*;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.mapper.PractitionerDashboardMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PractitionerDashboardService {

    private final IGenericClient fhirClient;
    private final PractitionerDashboardMapper mapper;

    public PractitionerDashboardService(IGenericClient fhirClient,
                                        PractitionerDashboardMapper mapper) {
        this.fhirClient = fhirClient;
        this.mapper = mapper;
    }

    // ─────────────────────────────────────────────────────────
    // EXISTING METHOD – leave as-is
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
            block.setEncounter(mapper.toEncounter(encounter));

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
                block.setOrganization(mapper.toOrganization(org));
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

                    block.setLocation(mapper.toLocation(loc, managingOrg));
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
                immBlock.setImmunization(mapper.toImmunization(imm));

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
                        pracDto = mapper.toPractitioner(practitioner);
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
                obsDtos.add(mapper.toObservation(obs, null));
            }
            block.setObservations(obsDtos);

            encounterBlocks.add(block);
        }

        overview.setEncounters(encounterBlocks);
        return overview;
    }

    // ===== NEW METHODS FOR PRACTITIONER DASHBOARD =====
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
            block.setEncounter(mapper.toEncounter(encounter));

            // —— Organization (serviceProvider) ——
            if (encounter.hasServiceProvider() &&
                    encounter.getServiceProvider().getReferenceElement().hasIdPart()) {

                String orgId = encounter.getServiceProvider().getReferenceElement().getIdPart();

                Organization org = fhirClient.read()
                        .resource(Organization.class)
                        .withId(orgId)
                        .execute();

                block.setOrganization(mapper.toOrganization(org));
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

                    block.setLocation(mapper.toLocation(loc, managingOrg));
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
                immBlock.setImmunization(mapper.toImmunization(imm));

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
                        pracDto = mapper.toPractitioner(practitioner);
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
                obsDtos.add(mapper.toObservation(obs, null));
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
            result.add(mapper.toAllergyIntoleranceDTO(ai));
        }

        return result;
    }


    /** Helper: current username from Spring Security (e.g. "dr.smith"). */
    private String getCurrentUsername() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    /** Helper: find Practitioner resource for currently logged-in user using identifier = username. */
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

    // ── 1) List patients assigned to this practitioner ────────────────────────

    public List<PatientDetailsDTO> getMyPatients() {
        Practitioner practitioner = getCurrentPractitioner();
        String practitionerId = practitioner.getIdElement().getIdPart();  // e.g. "123"

        Bundle bundle = fhirClient.search()
                .forResource(Patient.class)
                .where(Patient.GENERAL_PRACTITIONER.hasId("Practitioner/" + practitionerId))
                .returnBundle(Bundle.class)
                .execute();

        return bundle.getEntry().stream()
                .map(e -> (Patient) e.getResource())
                .map(this::toPatientSummaryDTO)
                .collect(Collectors.toList());
    }
    // ── REGISTER NEW PATIENT ────────────────────────────────────────

    public PatientDetailsDTO registerPatient(RegisterPatientRequestDTO request) {
        Practitioner practitioner = getCurrentPractitioner();
        String practitionerId = practitioner.getIdElement().getIdPart();

        Patient patient = new Patient();
        //patient.setId(request.getIdentifier());

        //  patient.addIdentifier()
        //     .setSystem("http://hospital.smarthealthit.org/patients")
        //   .setValue(request.getIdentifier());

        patient.addName()
                .addGiven(request.getFirstName())
                .setFamily(request.getLastName());

        if (request.getBirthDate() != null) {
            patient.setBirthDate(Date.from(
                    LocalDate.parse(request.getBirthDate())
                            .atStartOfDay(ZoneId.systemDefault())
                            .toInstant()
            ));
        }

        if (request.getGender() != null) {
            patient.setGender(Enumerations.AdministrativeGender.fromCode(request.getGender()));
        }

        patient.addGeneralPractitioner()
                .setReference("Practitioner/" + practitionerId);

        MethodOutcome outcome = fhirClient.create()
                .resource(patient)
                .execute();

        Patient created = (Patient) outcome.getResource();
        return toPatientSummaryDTO(created);
    }

    private PatientDetailsDTO toPatientSummaryDTO(Patient patient) {
        PatientDetailsDTO dto = new PatientDetailsDTO();
        dto.setPatientId(patient.getIdElement().getIdPart());

        if (patient.hasName() && !patient.getName().isEmpty()) {
            HumanName name = patient.getNameFirstRep();
            String given = name.getGivenAsSingleString();
            String family = name.getFamily();
            dto.setFirstName(given);
            dto.setLastName(family);
        }

        if (patient.hasBirthDate()) {
            LocalDate birth = patient.getBirthDate().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
            dto.setBirthDate(birth.toString());

        }

        if (patient.hasGender()) {
            dto.setGender(patient.getGender().toCode());
        }

        return dto;
    }

    // ── 2) Immunizations for a patient ───────────────────────────────────────

    public List<ImmunizationDTO> getImmunizationsForPatient(String patientId) {
        Bundle immBundle = fhirClient.search()
                .forResource(Immunization.class)
                .where(Immunization.PATIENT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        return immBundle.getEntry().stream()
                .map(e -> (Immunization) e.getResource())
                .map(this::toImmunizationDTO)
                .collect(Collectors.toList());
    }

    public ImmunizationDTO createImmunizationForPatient(String patientId,
                                                        CreateImmunizationRequest request) {

        Immunization imm = new Immunization();

        imm.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);
        imm.setPatient(new Reference("Patient/" + patientId));

        // Vaccine code
        CodeableConcept cc = new CodeableConcept();
        Coding coding = cc.addCoding();
        coding.setSystem("http://example.org/vaccine-codes");
        coding.setCode(request.getVaccineCode());
        coding.setDisplay(request.getVaccineDisplay());
        imm.setVaccineCode(cc);

        // Date
        if (request.getDate() != null) {
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
        return toImmunizationDTO(created);
    }

    private ImmunizationDTO toImmunizationDTO(Immunization imm) {
        ImmunizationDTO dto = new ImmunizationDTO();
        dto.setImmunizationId(imm.getIdElement().getIdPart());

        if (imm.hasPatient() && imm.getPatient().getReferenceElement().hasIdPart()) {
            dto.setPatientId(imm.getPatient().getReferenceElement().getIdPart());
        }

        if (imm.hasVaccineCode() && imm.getVaccineCode().hasCoding()) {
            Coding first = imm.getVaccineCode().getCodingFirstRep();
            dto.setVaccineCode(first.getCode());
            dto.setVaccineDisplay(first.getDisplay());
        }

        if (imm.hasOccurrenceDateTimeType()) {
            LocalDate date = imm.getOccurrenceDateTimeType().getValue().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            dto.setOccurrenceDateTime(date.toString());
        }

        if (imm.hasStatus()) {
            dto.setStatus(imm.getStatus().toCode());
        }


        if (imm.hasPerformer() && !imm.getPerformer().isEmpty()) {
            Reference actorRef = imm.getPerformerFirstRep().getActor();
            if (actorRef != null && actorRef.getReferenceElement().hasIdPart()) {
                String pracId = actorRef.getReferenceElement().getIdPart();
                Practitioner prac = fhirClient.read()
                        .resource(Practitioner.class)
                        .withId(pracId)
                        .execute();

            }
        }

        return dto;
    }

    // ── 3) Immunization recommendations ──────────────────────────────────────

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
                result.add(toRecommendationDTO(recId, patientId, comp));
            }
        }

        return result;
    }

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
        return toRecommendationDTO(recId, patientId, created.getRecommendationFirstRep());
    }

    private ImmunizationRecommendationDTO toRecommendationDTO(
            String recId,
            String patientId,
            ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent comp
    ) {
        ImmunizationRecommendationDTO dto = new ImmunizationRecommendationDTO();
        dto.setId(recId);
        dto.setPatientId(patientId);

        if (comp.hasVaccineCode()) {
            CodeableConcept cc = comp.getVaccineCodeFirstRep();
            if (cc.hasCoding()) {
                Coding first = cc.getCodingFirstRep();
                dto.setVaccineCode(first.getCode());
                dto.setVaccineDisplay(first.getDisplay());
            }
        }

        if (comp.hasDateCriterion() && !comp.getDateCriterion().isEmpty()) {
            var dc = comp.getDateCriterionFirstRep();
            if (dc.hasValue()) {
                LocalDate due = dc.getValue().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate();
                dto.setDueDate(due);
            }
        }

        if (comp.hasForecastStatus()) {
            dto.setStatus(comp.getForecastStatus().getText());
        }

        if (comp.hasSeries()) {
            dto.setSeries(comp.getSeries());
        }

        if (comp.hasDoseNumber()) {
            try {
                dto.setDoseNumber(Integer.valueOf(comp.getDoseNumber()));
            } catch (NumberFormatException e) {
                // leave null or handle error
            }
        }
        return dto;
    }

    // ── 4) Appointments ──────────────────────────────────────────────────────

    public List<AppointmentDTO> getAppointmentsForPatient(String patientId) {
        Bundle bundle = fhirClient.search()
                .forResource(Appointment.class)
                .where(Appointment.PATIENT.hasId(patientId))
                .returnBundle(Bundle.class)
                .execute();

        List<AppointmentDTO> result = new ArrayList<>();
        for (Bundle.BundleEntryComponent entry : bundle.getEntry()) {
            Appointment appt = (Appointment) entry.getResource();
            result.add(toAppointmentDTO(appt));
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
        return toAppointmentDTO(created);
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
        return toAppointmentDTO(updated);
    }

    private AppointmentDTO toAppointmentDTO(Appointment appt) {
        AppointmentDTO dto = new AppointmentDTO();
        dto.setId(appt.getIdElement().getIdPart());

        // Start / end
        if (appt.hasStart()) {
            LocalDateTime start = appt.getStart().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            dto.setStart(start);
        }
        if (appt.hasEnd()) {
            LocalDateTime end = appt.getEnd().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();
            dto.setEnd(end);
        }

        if (appt.hasStatus()) {
            dto.setStatus(appt.getStatus().toCode());
        }

        // Reason (very simplified)
        if (appt.hasReason() && !appt.getReason().isEmpty()) {
            CodeableReference ref = appt.getReasonFirstRep();
            if (ref.hasConcept() && ref.getConcept().hasText()) {
                dto.setReason(ref.getConcept().getText());
            }
        }

        // Location: we stored as description above
        if (appt.hasDescription()) {
            dto.setLocation(appt.getDescription());
        }

        // Get patient & practitioner names if available
        for (Appointment.AppointmentParticipantComponent part : appt.getParticipant()) {
            if (!part.hasActor() || !part.getActor().getReferenceElement().hasIdPart()) {
                continue;
            }
            Reference actorRef = part.getActor();
            String ref = actorRef.getReference();

            if (ref != null) {
                if (ref.startsWith("Patient/")) {
                    String patientId = actorRef.getReferenceElement().getIdPart();
                    dto.setPatientId(patientId);

                    Patient patient = fhirClient.read()
                            .resource(Patient.class)
                            .withId(patientId)
                            .execute();
                    if (patient.hasName()) {
                        HumanName name = patient.getNameFirstRep();
                        String fullName = (name.getGivenAsSingleString() + " " + name.getFamily()).trim();
                        dto.setPatientName(fullName);
                    }
                } else if (ref.startsWith("Practitioner/")) {
                    String pracId = actorRef.getReferenceElement().getIdPart();
                    dto.setPractitionerId(pracId);

                    Practitioner prac = fhirClient.read()
                            .resource(Practitioner.class)
                            .withId(pracId)
                            .execute();
                    if (prac.hasName()) {
                        HumanName name = prac.getNameFirstRep();
                        String fullName = (name.getGivenAsSingleString() + " " + name.getFamily()).trim();
                        dto.setPractitionerName(fullName);
                    }
                }
            }
        }

        return dto;
    }

    public void createFullEncounter(String patientId, CreateFullEncounterRequest request) {

        // ----------------------------------------
        // 1) CREATE ENCOUNTER
        // ----------------------------------------
        Encounter encounter = new Encounter();

        encounter.setId("Encounter/" + request.getEncounterId());
        encounter.setStatus(Enumerations.EncounterStatus.COMPLETED);
        encounter.setSubject(new Reference("Patient/" + patientId));

        // Encounter date
        if (request.getEncounterDate() != null) {
            Period period = new Period();
            period.setStart(Date.from(
                    LocalDateTime.parse(request.getEncounterDate())
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
            ));

            encounter.setActualPeriod(period);
        }

        // Organization
        if (request.getOrganizationId() != null) {
            encounter.setServiceProvider(
                    new Reference("Organization/" + request.getOrganizationId())
            );
        }

        // Location
        if (request.getLocationId() != null) {
            encounter.addLocation().setLocation(
                    new Reference("Location/" + request.getLocationId())
            );
        }

        fhirClient.update().resource(encounter).execute();


        // ----------------------------------------
        // 2) CREATE IMMUNIZATIONS
        // ----------------------------------------
        if (request.getImmunizations() != null) {
            for (var immInput : request.getImmunizations()) {

                Immunization imm = new Immunization();

                imm.setId("Immunization/" + immInput.getImmunizationId());
                imm.setStatus(Immunization.ImmunizationStatusCodes.COMPLETED);
                imm.setPatient(new Reference("Patient/" + patientId));
                imm.setEncounter(new Reference("Encounter/" + request.getEncounterId()));

                imm.setVaccineCode(
                        new CodeableConcept().addCoding(
                                new Coding()
                                        .setSystem("http://hl7.org/fhir/sid/cvx")
                                        .setCode(immInput.getVaccineCode())
                                        .setDisplay(immInput.getVaccineDisplay())
                        )
                );

                if (immInput.getOccurrenceDateTime() != null) {
                    imm.setOccurrence(
                            new DateTimeType(immInput.getOccurrenceDateTime())
                    );
                }

                fhirClient.update().resource(imm).execute();
            }
        }


        // ----------------------------------------
        // 3) CREATE OBSERVATIONS
        // ----------------------------------------
        if (request.getObservations() != null) {
            for (var obsInput : request.getObservations()) {

                Observation obs = new Observation();
                obs.setId("Observation/" + obsInput.getObservationId());

                obs.setStatus(Enumerations.ObservationStatus.FINAL);
                obs.setSubject(new Reference("Patient/" + patientId));
                obs.setEncounter(new Reference("Encounter/" + request.getEncounterId()));

                obs.setCode(new CodeableConcept().addCoding(
                        new Coding()
                                .setCode(obsInput.getCode())
                                .setDisplay(obsInput.getDisplay())
                ));

                obs.setValue(new Quantity()
                        .setValue(Double.parseDouble(obsInput.getValue()))
                        .setUnit(obsInput.getUnit())
                );

                if (obsInput.getEffectiveDateTime() != null) {
                    obs.setEffective(new DateTimeType(obsInput.getEffectiveDateTime()));
                }

                fhirClient.update().resource(obs).execute();
            }
        }
    }

}
