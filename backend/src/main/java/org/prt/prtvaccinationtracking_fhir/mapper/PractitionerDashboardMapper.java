package org.prt.prtvaccinationtracking_fhir.mapper;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.springframework.stereotype.Component;

@Component
public class PractitionerDashboardMapper {

    // ---------------- Patient / RelatedPerson ----------------
    public PatientDetailsDTO toPatientDetails(Patient patient) {
        PatientDetailsDTO dto = new PatientDetailsDTO();

        // Patient.id (technical FHIR ID)
        dto.setPatientId(patient.getIdElement().getIdPart());

        // Business identifier – take the first identifier if present
        if (patient.hasIdentifier() && !patient.getIdentifier().isEmpty()) {
            dto.setPatientIdentifier(patient.getIdentifierFirstRep().getValue());
        }

        // Name – first name + last name
        if (patient.hasName() && !patient.getName().isEmpty()) {
            HumanName name = patient.getNameFirstRep();
            dto.setFirstName(name.getGivenAsSingleString()); // all given names joined
            dto.setLastName(name.getFamily());
        }

        // Birth date
        if (patient.hasBirthDate()) {
            dto.setBirthDate(patient.getBirthDateElement().asStringValue());
        }

        // Gender
        if (patient.hasGender()) {
            dto.setGender(patient.getGender().toCode()); // "male", "female", "other", "unknown"
        }

        return dto;
    }

    // ---------------- Encounter / Organization / Location ----------------

    public EncounterDTO toEncounter(Encounter enc) {
        EncounterDTO dto = new EncounterDTO();

        // FHIR Encounter.id
        dto.setEncounterId(enc.getIdElement().getIdPart());

        // Subject = patient
        if (enc.hasSubject() && enc.getSubject().getReferenceElement().hasIdPart()) {
            dto.setPatientId(enc.getSubject().getReferenceElement().getIdPart());
        }

        // Status (planned | in-progress | finished )
        if (enc.hasStatus()) {
            dto.setStatus(enc.getStatus().toCode());
        }
        // Period
        if (enc.hasActualPeriod()) {
            Period p = enc.getActualPeriod();

            if (p.hasStart()) {
                dto.setStartDateTime(p.getStartElement().asStringValue());
            }
            if (p.hasEnd()) {
                dto.setEndDateTime(p.getEndElement().asStringValue());
            }
        }

        return dto;
    }

    public OrganizationDTO toOrganization(Organization org) {
        OrganizationDTO dto = new OrganizationDTO();

        // FHIR Organization.id
        dto.setOrganizationId(org.getIdElement().getIdPart());

        // Organization name
        dto.setName(org.getName());

        return dto;
    }

    public LocationDTO toLocation(Location loc, Organization managingOrgOrNull) {
        LocationDTO dto = new LocationDTO();

        // FHIR Location.id
        dto.setLocationId(loc.getIdElement().getIdPart());

        // Name of the location
        dto.setName(loc.getName());

        // Address
        if (loc.hasAddress()) {
            dto.setAddress(loc.getAddress().getText());
        }

        // Description
        if (loc.hasDescription()) {
            dto.setDescription(loc.getDescription());
        }

        // Link to owning organization (by id only, since DTO has only organizationId)
        if (managingOrgOrNull != null) {
            dto.setOrganizationId(managingOrgOrNull.getIdElement().getIdPart());
        }

        return dto;
    }

    // ---------------- Immunization / Practitioner ----------------

    public ImmunizationDTO toImmunization(Immunization imm) {
        ImmunizationDTO dto = new ImmunizationDTO();

        // 1) IDs
        dto.setImmunizationId(imm.getIdElement().getIdPart());

        if (imm.hasPatient() && imm.getPatient().getReferenceElement().hasIdPart()) {
            dto.setPatientId(imm.getPatient().getReferenceElement().getIdPart());
        }

        if (imm.hasEncounter() && imm.getEncounter().getReferenceElement().hasIdPart()) {
            dto.setEncounterId(imm.getEncounter().getReferenceElement().getIdPart());
        }

        // 2) Vaccine code & display
        if (imm.hasVaccineCode()) {
            CodeableConcept cc = imm.getVaccineCode();
            if (!cc.getCoding().isEmpty()) {
                dto.setVaccineCode(cc.getCodingFirstRep().getCode());
                dto.setVaccineDisplay(cc.getCodingFirstRep().getDisplay());
            } else {
                dto.setVaccineDisplay(cc.getText());
            }
        }

        // 3) Occurrence date
        if (imm.hasOccurrenceDateTimeType()) {
            dto.setOccurrenceDateTime(imm.getOccurrenceDateTimeType().asStringValue());
        }

        // 4) Status
        if (imm.hasStatus()) {
            dto.setStatus(imm.getStatus().toCode());
        }

        return dto;
    }

    public PractitionerDTO toPractitioner(Practitioner practitioner) {
        PractitionerDTO dto = new PractitionerDTO();

        // FHIR Practitioner.id
        dto.setPractitionerId(practitioner.getIdElement().getIdPart());

        // Business identifier (first identifier)
        if (practitioner.hasIdentifier() && !practitioner.getIdentifier().isEmpty()) {
            dto.setPractitionerIdentifier(
                    practitioner.getIdentifierFirstRep().getValue()
            );
        }

        // Name → firstName / lastName
        if (practitioner.hasName() && !practitioner.getName().isEmpty()) {
            HumanName name = practitioner.getNameFirstRep();
            dto.setFirstName(name.getGivenAsSingleString()); // all given names joined
            dto.setLastName(name.getFamily());
        }

        return dto;
    }

    // ---------------- Observation ----------------

    public ObservationDTO toObservation(Observation obs, String immunizationIdOrNull) {
        ObservationDTO dto = new ObservationDTO();

        dto.setObservationId(obs.getIdElement().getIdPart());
        dto.setImmunizationId(immunizationIdOrNull);

        if (obs.hasCode()) {
            CodeableConcept cc = obs.getCode();
            if (!cc.getCoding().isEmpty()) {
                dto.setCode(cc.getCodingFirstRep().getCode());
                dto.setDisplay(cc.getCodingFirstRep().getDisplay());
            } else {
                dto.setDisplay(cc.getText());
            }
        }

        if (obs.hasValueQuantity()) {
            Quantity q = obs.getValueQuantity();
            if (q.hasValue()) {
                dto.setValue(q.getValueElement().asStringValue());
            }
            dto.setUnit(q.getUnit());
        } else if (obs.hasValueStringType()) {
            dto.setValue(obs.getValueStringType().asStringValue());
        }

        if (obs.hasEffectiveDateTimeType()) {
            dto.setEffectiveDateTime(obs.getEffectiveDateTimeType().asStringValue());
        }

        return dto;
    }

    public AllergyIntoleranceDTO toAllergyIntoleranceDTO(AllergyIntolerance ai) {

        AllergyIntoleranceDTO dto = new AllergyIntoleranceDTO();

        dto.setAllergyId(ai.getIdElement().getIdPart());

        if (ai.hasClinicalStatus()) {
            dto.setClinicalStatus(ai.getClinicalStatus().getText());
        }

        if (ai.hasVerificationStatus()) {
            dto.setVerificationStatus(ai.getVerificationStatus().getText());
        }

        if (ai.hasCode() && ai.getCode().hasCoding()) {
            Coding c = ai.getCode().getCodingFirstRep();
            dto.setCode(c.getCode());
            dto.setDisplay(c.getDisplay());
        }

        if (ai.hasCriticality()) {
            dto.setCriticality(ai.getCriticality().toCode());
        }

        if (ai.hasReaction() && !ai.getReaction().isEmpty()) {
            var r = ai.getReactionFirstRep();
            if (r.hasDescription()) {
                dto.setReaction(r.getDescription());
            }
        }

        return dto;
    }

}
