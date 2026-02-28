package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.CreateImmunizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.ImmunizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.ImmunizationStatusDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.UpdateImmunizationRequestDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface ImmunizationMapper {

    String CVX_SYSTEM = "http://hl7.org/fhir/sid/cvx";

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getPatient() != null ? resource.getPatient().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "vaccineCode", expression = "java(extractVaccineCode(resource))")
    @Mapping(target = "vaccineDisplay", expression = "java(extractVaccineDisplay(resource))")
    @Mapping(target = "administrationDate", expression = "java(toLocalDate(extractOccurrenceDateTime(resource)))")
    @Mapping(target = "doseNumber", expression = "java(extractDoseNumber(resource))")
    @Mapping(target = "lotNumber", expression = "java(resource.getLotNumber())")
    @Mapping(target = "site", expression = "java(resource.hasSite() ? resource.getSite().getText() : null)")
    @Mapping(target = "status", expression = "java(resource.getStatus() != null ? ImmunizationStatusDTO.fromFhirCode(resource.getStatus().toCode()) : null)")
    @Mapping(target = "practitionerName", expression = "java(extractPractitionerName(resource))")
    @Mapping(target = "encounterId", expression = "java(resource.getEncounter() != null ? resource.getEncounter().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "hasAdverseEvent", expression = "java(false)")
    ImmunizationDTO toDTO(Immunization resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(dto.status() != null ? Immunization.ImmunizationStatus.fromCode(dto.status().toFhirCode()) : null)")
    @Mapping(target = "patient", expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "vaccineCode", expression = "java(toVaccineCode(dto.vaccineCode(), dto.vaccineDisplay()))")
    @Mapping(target = "occurrence", expression = "java(dto.administrationDate() != null ? new DateTimeType(toDate(dto.administrationDate())) : null)")
    @Mapping(target = "lotNumber", expression = "java(dto.lotNumber())")
    @Mapping(target = "site", expression = "java(dto.site() != null ? new CodeableConcept().setText(dto.site()) : null)")
    @Mapping(target = "protocolApplied", expression = "java(dto.doseNumber() != null ? toProtocolApplied(dto.doseNumber()) : Collections.emptyList())")
    @Mapping(target = "encounter", expression = "java(dto.encounterId() != null ? new Reference(\"Encounter/\" + dto.encounterId()) : null)")
    Immunization toResource(CreateImmunizationRequestDTO dto);

    @Mapping(target = "lotNumber", expression = "java(dto.lotNumber() != null ? dto.lotNumber() : resource.getLotNumber())")
    @Mapping(target = "site", expression = "java(dto.site() != null ? new CodeableConcept().setText(dto.site()) : resource.getSite())")
    @Mapping(target = "status", expression = "java(dto.status() != null ? Immunization.ImmunizationStatus.fromCode(dto.status().toFhirCode()) : resource.getStatus())")
    @Mapping(target = "note", expression = "java(dto.notes() != null ? toNotes(dto.notes()) : resource.getNote())")
    void updateResourceFromDTO(UpdateImmunizationRequestDTO dto, @MappingTarget Immunization resource);

    default CodeableConcept toVaccineCode(String code, String display) {
        CodeableConcept cc = new CodeableConcept();
        if (code != null) cc.addCoding(new Coding().setSystem(CVX_SYSTEM).setCode(code).setDisplay(display));
        if (display != null) cc.setText(display);
        return cc;
    }

    default DateTimeType extractOccurrenceDateTime(Immunization resource) {
        if (resource == null || !resource.hasOccurrence()) return null;
        if (resource.getOccurrence() instanceof DateTimeType dt) return dt;
        return null;
    }

    default String extractVaccineCode(Immunization resource) {
        if (resource == null || !resource.hasVaccineCode()) return null;
        CodeableConcept cc = resource.getVaccineCode();
        for (Coding c : cc.getCoding()) {
            if (CVX_SYSTEM.equals(c.getSystem()) && c.hasCode()) return c.getCode();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractVaccineDisplay(Immunization resource) {
        if (resource == null || !resource.hasVaccineCode()) return null;
        CodeableConcept cc = resource.getVaccineCode();
        for (Coding c : cc.getCoding()) {
            if (CVX_SYSTEM.equals(c.getSystem()) && c.hasDisplay()) return c.getDisplay();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default Integer extractDoseNumber(Immunization resource) {
        if (resource == null || !resource.hasProtocolApplied() || resource.getProtocolApplied().isEmpty()) return null;
        Immunization.ImmunizationProtocolAppliedComponent p = resource.getProtocolAppliedFirstRep();
        if (!p.hasDoseNumber()) return null;
        String v = p.getDoseNumber();
        if (v == null) return null;
        try {
            return Integer.parseInt(v);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    default List<Immunization.ImmunizationProtocolAppliedComponent> toProtocolApplied(Integer doseNumber) {
        Immunization.ImmunizationProtocolAppliedComponent p = new Immunization.ImmunizationProtocolAppliedComponent();
        p.setDoseNumber(String.valueOf(doseNumber));
        return Collections.singletonList(p);
    }

    default List<Annotation> toNotes(String notes) {
        return Collections.singletonList(new Annotation().setText(notes));
    }

    default String extractPractitionerName(Immunization resource) {
        if (resource == null || !resource.hasPerformer() || resource.getPerformer().isEmpty()) return null;
        Immunization.ImmunizationPerformerComponent perf = resource.getPerformerFirstRep();
        if (perf.hasActor() && perf.getActor().hasDisplay()) return perf.getActor().getDisplay();
        return null;
    }

    default LocalDate toLocalDate(DateTimeType type) {
        if (type == null || type.getValue() == null) return null;
        return type.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    default Date toDate(LocalDate date) {
        if (date == null) return null;
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}