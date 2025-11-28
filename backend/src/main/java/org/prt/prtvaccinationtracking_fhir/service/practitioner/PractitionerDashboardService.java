package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.ReferenceClientParam;
import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.mapper.PractitionerDashboardMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class PractitionerDashboardService {

    private final IGenericClient client;
    private final PractitionerDashboardMapper mapper;

    // ---- constructor injection ----
    public PractitionerDashboardService(IGenericClient client,
                                        PractitionerDashboardMapper mapper) {
        this.client = client;
        this.mapper = mapper;
    }

    /**
     * Build the full clinical overview for one patient (child).
     */
    public PatientClinicalOverviewDTO getPatientClinicalOverview(String patientId) {

        PatientClinicalOverviewDTO overview = new PatientClinicalOverviewDTO();

        // 1) Patient (child)
        Patient patient = client.read()
                .resource(Patient.class)
                .withId(patientId)
                .execute();
        overview.setPatient(mapper.toPatientDetails(patient));

        // 2) Related persons (parents / guardians)
        List<RelatedPersonDTO> relatedPersons = new ArrayList<>();
        Bundle rpBundle = client.search()
                .forResource(RelatedPerson.class)
                .where(RelatedPerson.PATIENT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        for (Bundle.BundleEntryComponent rpEntry : rpBundle.getEntry()) {
            RelatedPerson rp = (RelatedPerson) rpEntry.getResource();
            relatedPersons.add(mapper.toRelatedPerson(rp));
        }
        overview.setRelatedPersons(relatedPersons);

        // 3) Encounters for this child
        List<EncounterBlockDTO> encounterBlocks = new ArrayList<>();
        Bundle encBundle = client.search()
                .forResource(Encounter.class)
                .where(Encounter.SUBJECT.hasId("Patient/" + patientId))
                .returnBundle(Bundle.class)
                .execute();

        for (Bundle.BundleEntryComponent encEntry : encBundle.getEntry()) {
            Encounter encounter = (Encounter) encEntry.getResource();
            EncounterBlockDTO block = new EncounterBlockDTO();

            // 3a) Encounter core
            block.setEncounter(mapper.toEncounter(encounter));
            String encId = encounter.getIdElement().getIdPart();

            // 3b) Organization (Encounter.serviceProvider)
            if (encounter.hasServiceProvider()
                    && encounter.getServiceProvider().getReferenceElement().hasIdPart()) {

                String orgId = encounter.getServiceProvider().getReferenceElement().getIdPart();
                Organization org = client.read()
                        .resource(Organization.class)
                        .withId(orgId)
                        .execute();
                block.setOrganization(mapper.toOrganization(org));
            }

            // 3c) Location (Encounter.location[0] + its managing organization)
            if (encounter.hasLocation() && !encounter.getLocation().isEmpty()) {
                Reference locRef = encounter.getLocationFirstRep().getLocation();
                if (locRef != null && locRef.getReferenceElement().hasIdPart()) {
                    String locId = locRef.getReferenceElement().getIdPart();
                    Location loc = client.read()
                            .resource(Location.class)
                            .withId(locId)
                            .execute();

                    Organization managingOrg = null;
                    if (loc.hasManagingOrganization()
                            && loc.getManagingOrganization().getReferenceElement().hasIdPart()) {
                        String locOrgId = loc.getManagingOrganization().getReferenceElement().getIdPart();
                        managingOrg = client.read()
                                .resource(Organization.class)
                                .withId(locOrgId)
                                .execute();
                    }

                    block.setLocation(mapper.toLocation(loc, managingOrg));
                }
            }

            // 3d) Immunizations in this encounter (+ practitioner)
            List<ImmunizationBlockDTO> immBlocks = new ArrayList<>();

            Bundle immBundle = client.search()
                    .forResource(Immunization.class)
                    .where(Immunization.PATIENT.hasId("Patient/" + patientId))
                    // use generic reference param instead of Immunization.ENCOUNTER
                    .and(new ReferenceClientParam("encounter")
                            .hasId("Encounter/" + encId))
                    .returnBundle(Bundle.class)
                    .execute();

            for (Bundle.BundleEntryComponent immEntry : immBundle.getEntry()) {
                Immunization imm = (Immunization) immEntry.getResource();
                ImmunizationBlockDTO immBlock = new ImmunizationBlockDTO();

                // Immunization data
                immBlock.setImmunization(mapper.toImmunization(imm));

                // Practitioner who performed it
                PractitionerDTO pracDto = null;
                if (imm.hasPerformer() && !imm.getPerformer().isEmpty()) {
                    Reference actorRef = imm.getPerformerFirstRep().getActor();
                    if (actorRef != null && actorRef.getReferenceElement().hasIdPart()) {
                        String pracId = actorRef.getReferenceElement().getIdPart();
                        Practitioner practitioner = client.read()
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

            // 3e) All observations for this encounter
            List<ObservationDTO> obsDtos = new ArrayList<>();
            Bundle obsBundle = client.search()
                    .forResource(Observation.class)
                    .where(new ReferenceClientParam("encounter")
                            .hasId("Encounter/" + encId))
                    .returnBundle(Bundle.class)
                    .execute();

            for (Bundle.BundleEntryComponent obsEntry : obsBundle.getEntry()) {
                Observation obs = (Observation) obsEntry.getResource();
                obsDtos.add(mapper.toObservation(obs, null)); // encounter-level obs
            }
            block.setObservations(obsDtos);

            encounterBlocks.add(block);
        }

        overview.setEncounters(encounterBlocks);
        return overview;
    }
}
