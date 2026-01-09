package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.springframework.stereotype.Component;

@Component
public class PractitionerDashboardMapper {

    private static final String SVNR_SYSTEM = "https://elga.gv.at/svnr";
    private static final String PROFILE_PATIENT_CHILD =
            "http://example.org/fhir/StructureDefinition/patient-child";
    private static final String PROFILE_RELATEDPERSON_GUARDIAN =
            "http://example.org/fhir/StructureDefinition/relatedperson-guardian";

    private final PatientMapper patientMapper;

    public PractitionerDashboardMapper(PatientMapper patientMapper) {
        this.patientMapper = patientMapper;
    }

    // ─────────────────────────────────────────
    // DASHBOARD: Patient → DTO
    // ─────────────────────────────────────────
    public PatientDetailsDTO toPatientDetails(Patient patient) {

        PatientDetailsDTO dto = new PatientDetailsDTO();

        dto.setPatientId(patient.getIdElement().getIdPart());

        /// Business identifier (SVNR)
        if (patient.hasIdentifier()) {
            ///  picks the identifier that matches SVNR_SYSTEM
            dto.setPatientIdentifier(patient.getIdentifierFirstRep().getValue());
        }

        /// Name
        if (patient.hasName()) {
            HumanName name = patient.getNameFirstRep();
            if (!name.getGiven().isEmpty()) {
                dto.setFirstName(name.getGiven().get(0).getValue());
            }
            dto.setLastName(name.getFamily());
        }

        /// Birth date
        if (patient.hasBirthDate()) {
            dto.setBirthDate(patient.getBirthDate().toString());
        }

        /// Gender
        if (patient.hasGender()) {
            dto.setGender(patient.getGender().toCode());
        }

        return dto;
    }

    // ─────────────────────────────────────────
    // DASHBOARD: RelatedPerson → DTO
    // ─────────────────────────────────────────
    public RelatedPersonDTO toRelatedPerson(RelatedPerson rp) {

        RelatedPersonDTO dto = new RelatedPersonDTO();

        dto.setRelatedPersonId(rp.getIdElement().getIdPart());

        if (rp.hasIdentifier()) {
            dto.setRelatedPersonIdentifier(rp.getIdentifierFirstRep().getValue());
        }

        // Relationship
        if (rp.hasRelationship()) {
            // Prefer coding if present; fallback to text
            CodeableConcept rel = rp.getRelationshipFirstRep();
            if (rel.hasCoding()) {
                dto.setRelationship(rel.getCodingFirstRep().getCode());
            } else {
                dto.setRelationship(rel.getText());
            }
        }

        // Full name
        if (rp.hasName()) {
            HumanName name = rp.getNameFirstRep();

            String given = name.hasGiven()
                    ? name.getGiven().stream()
                    .map(PrimitiveType::getValue)
                    .reduce("", (a, b) -> a + " " + b).trim()
                    : "";

            String family = name.hasFamily() ? name.getFamily() : "";

            dto.setFullName((given + " " + family).trim());
        }

        // Telecom (phone/email)
        if (rp.hasTelecom()) {
            for (ContactPoint cp : rp.getTelecom()) {
                if (cp.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                    dto.setPhone(cp.getValue());
                }
                if (cp.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                    dto.setEmail(cp.getValue());
                }
            }
        }

        // Address
        if (rp.hasAddress()) {
            Address addr = rp.getAddressFirstRep();
            dto.setAddress(
                    addr.getText() != null
                            ? addr.getText()
                            : String.join(", ",
                            addr.getLine().stream()
                                    .map(PrimitiveType::getValue)
                                    .toList()
                    )
            );
        }

        return dto;
    }

    // ─────────────────────────────────────────
    // CREATE Patient (delegates to PatientMapper)
    // ─────────────────────────────────────────
    public Patient toPatient(CreatePatientRequestDTO dto) {
        Patient patient = patientMapper.toPatient(dto);

        // Add profile here (app-specific constraint)
        patient.getMeta().addProfile(PROFILE_PATIENT_CHILD);

        return patient;
    }

    // ─────────────────────────────────────────
    // UPDATE Patient (delegates to PatientMapper)
    // ─────────────────────────────────────────
    public void updatePatient(Patient patient, UpdatePatientRequestDTO dto) {
        patientMapper.updatePatient(patient, dto);
    }

    // ─────────────────────────────────────────
    // CREATE RelatedPerson
    // ─────────────────────────────────────────
    public RelatedPerson toRelatedPerson(CreateRelatedPersonRequestDTO dto) {

        RelatedPerson rp = new RelatedPerson();

        // Link to child
        rp.setPatient(new Reference("Patient/" + dto.getPatientId()));

        // Add profile
        rp.getMeta().addProfile(PROFILE_RELATEDPERSON_GUARDIAN);

        // SVNR
        rp.addIdentifier()
                .setSystem(SVNR_SYSTEM)
                .setValue(dto.getRelatedPersonIdentifier());

        // Relationship (ValueSet compliant codes: e.g. MTH/FTH/GPAR)
        rp.addRelationship()
                .addCoding(new Coding()
                        .setSystem("http://terminology.hl7.org/CodeSystem/v3-RoleCode")
                        .setCode(dto.getRelationship())
                        .setDisplay(dto.getRelationship())
                );

        // Name
        rp.addName().setText(dto.getFullName());

        // Telecom
        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            rp.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(dto.getPhone());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            rp.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(dto.getEmail());
        }

        // Address
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            rp.addAddress().setText(dto.getAddress());
        }

        return rp;
    }

    // ─────────────────────────────────────────
    // UPDATE RelatedPerson
    // ─────────────────────────────────────────
    public void updateRelatedPerson(RelatedPerson rp, UpdateRelatedPersonRequestDTO dto) {

        // Relationship
        if (rp.hasRelationship()) {
            CodeableConcept rel = rp.getRelationshipFirstRep();
            rel.getCoding().clear();
            rel.addCoding(new Coding()
                    .setSystem("http://terminology.hl7.org/CodeSystem/v3-RoleCode")
                    .setCode(dto.getRelationship())
                    .setDisplay(dto.getRelationship())
            );
            rel.setText(null);
        } else {
            rp.addRelationship()
                    .addCoding(new Coding()
                            .setSystem("http://terminology.hl7.org/CodeSystem/v3-RoleCode")
                            .setCode(dto.getRelationship())
                            .setDisplay(dto.getRelationship())
                    );
        }

        // Name
        if (rp.hasName()) {
            rp.getNameFirstRep().setText(dto.getFullName());
        } else {
            rp.addName().setText(dto.getFullName());
        }

        // Telecom: replace phone/email cleanly
        rp.getTelecom().clear();

        if (dto.getPhone() != null && !dto.getPhone().isBlank()) {
            rp.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(dto.getPhone());
        }

        if (dto.getEmail() != null && !dto.getEmail().isBlank()) {
            rp.addTelecom()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(dto.getEmail());
        }

        // Address
        if (dto.getAddress() != null && !dto.getAddress().isBlank()) {
            if (rp.hasAddress()) {
                rp.getAddressFirstRep().setText(dto.getAddress());
            } else {
                rp.addAddress().setText(dto.getAddress());
            }
        }
    }
}
