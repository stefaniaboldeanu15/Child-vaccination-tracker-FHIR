package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class PractitionerPatientOverviewMapper {

    public PractitionerPatientOverviewMapper(PatientMapper patientMapper) {
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

        //FHIR organization.id
        dto.setOrganizationId(org.getIdElement().getIdPart());

        // Organization name
        dto.setName(org.getName());

        return dto;
    }

    public LocationDTO toLocation(Location loc, Organization managingOrgOrNull) {
        LocationDTO dto = new LocationDTO();

        //FHIR Location.id
        dto.setLocationId(loc.getIdElement().getIdPart());

        //Name of the location
        dto.setName(loc.getName());

        //Address
        if (loc.hasAddress()) {
            dto.setAddress(loc.getAddress().getText());
        }

        //Description
        if (loc.hasDescription()) {
           dto.setDescription(loc.getDescription());
        }
        // Link to owning organization (by id only, since DTO has only organizationId)
        if (managingOrgOrNull != null) {
            dto.setOrganizationId(managingOrgOrNull.getIdElement().getIdPart());
        }
        return dto;
    }

    //---------------- Immunization/ Practitioner / Organization / Location
    public ImmunizationDTO toImmunization (Immunization imm) {
        ImmunizationDTO dto =new ImmunizationDTO();

        // Immunization.id + Patient Id + Encounter id
        dto.setImmunizationId(imm.getIdElement().getIdPart());

        if (imm.hasPatient() && imm.getPatient().getReferenceElement().hasIdPart()) {
            dto.setPatientId(imm.getPatient().getReferenceElement().getIdPart());
        }

        if (imm.hasEncounter() && imm.getEncounter().getReferenceElement().hasIdPart()){
            dto.setEncounterId(imm.getEncounter().getReferenceElement().getIdPart());
        }

        // Vaccine code + display
        if (imm.hasVaccineCode()) {
            CodeableConcept vaccine = imm.getVaccineCode();
            if(!vaccine.getCoding().isEmpty()) {
                dto.setVaccineCode(vaccine.getCodingFirstRep().getCode());
                dto.setVaccineDisplay(vaccine.getCodingFirstRep().getDisplay());
            } else {
                dto.setVaccineDisplay(vaccine.getText());
            }
        }
        //Occurrence date
        if (imm.hasOccurrenceDateTimeType()){
            dto.setOccurrenceDateTime(imm.getOccurrenceDateTimeType().asStringValue());
        }
        //Status
        if (imm.hasStatus()){
            dto.setStatus(imm.getStatus().toCode());
        }
        return dto;

    }

    public ObservationDTO toObservation (Observation obs, String immunizationIdorNull){
        ObservationDTO dto = new ObservationDTO();
        dto.setObservationId(obs.getIdElement().getIdPart());
        dto.setImmunizationId(immunizationIdorNull);

        if(obs.hasCode()){
            CodeableConcept cc = obs.getCode();
            if(!cc.getCoding().isEmpty()) {
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

    public AllergyIntoleranceDTO toAllergyIntolerance(AllergyIntolerance ai) {

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

    //-----------------Immunization recommendation--------------------
    public ImmunizationRecommendationDTO toRecommendationDTO(
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
    //-----------------Appointment--------------------
    public AppointmentDTO toAppointmentDTO(Appointment appt) {
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
        return dto;
    }




}
