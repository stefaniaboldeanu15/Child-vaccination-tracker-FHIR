package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.CreateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.ImmunizationRecommendationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.UpdateImmunizationRecommendationRequestDTO;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface RecommendationMapper {

    String CVX_SYSTEM = "http://hl7.org/fhir/sid/cvx";
    String DUE_CRITERION_TEXT = "due";

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getPatient() != null ? resource.getPatient().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "date", expression = "java(toLocalDateTime(resource.getDate()))")
    @Mapping(target = "vaccineCode", expression = "java(extractVaccineCode(resource))")
    @Mapping(target = "vaccineDisplay", expression = "java(extractVaccineDisplay(resource))")
    @Mapping(target = "status", expression = "java(extractStatus(resource))")
    @Mapping(target = "dueDate", expression = "java(extractDueDate(resource))")
    @Mapping(target = "recommendationSource", expression = "java(resource.getAuthority() != null ? resource.getAuthority().getDisplay() : null)")
    ImmunizationRecommendationDTO toDTO(ImmunizationRecommendation resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "date", expression = "java(dto.date() != null ? toDate(dto.date()) : null)")
    @Mapping(target = "recommendation", expression = "java(toRecommendationList(dto))")
    ImmunizationRecommendation toResource(CreateImmunizationRecommendationRequestDTO dto);

    @Mapping(target = "patient", expression = "java(resource.getPatient())")
    @Mapping(target = "date", expression = "java(resource.getDate())")
    @Mapping(target = "recommendation", expression = "java(updateRecommendationList(resource, dto))")
    void updateResourceFromDTO(UpdateImmunizationRecommendationRequestDTO dto, @MappingTarget ImmunizationRecommendation resource);

    default List<ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> toRecommendationList(CreateImmunizationRecommendationRequestDTO dto) {
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r =
                new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent();

        r.setVaccineCode(Collections.singletonList(toVaccineCode(dto.vaccineCode(), dto.vaccineDisplay())));
        r.setForecastStatus(new CodeableConcept().setText(dto.status()));

        if (dto.dueDate() != null) {
            r.setDateCriterion(toDueDateCriterion(dto.dueDate()));
        }

        return Collections.singletonList(r);
    }

    default List<ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> updateRecommendationList(
            ImmunizationRecommendation resource,
            UpdateImmunizationRecommendationRequestDTO dto
    ) {
        List<ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent> list =
                resource.hasRecommendation() ? resource.getRecommendation() : new java.util.ArrayList<>();

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r =
                list.isEmpty()
                        ? new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent()
                        : list.get(0);

        if (dto.status() != null) {
            r.setForecastStatus(new CodeableConcept().setText(dto.status()));
        } else if (!r.hasForecastStatus()) {
            r.setForecastStatus(new CodeableConcept().setText("due"));
        }

        if (dto.dueDate() != null) {
            r.setDateCriterion(toDueDateCriterion(dto.dueDate()));
        }

        if (list.isEmpty()) {
            list.add(r);
        }
        return list;
    }

    default CodeableConcept toVaccineCode(String code, String display) {
        CodeableConcept cc = new CodeableConcept();
        if (code != null) cc.addCoding(new Coding().setSystem(CVX_SYSTEM).setCode(code).setDisplay(display));
        if (display != null) cc.setText(display);
        return cc;
    }

    default List<ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent> toDueDateCriterion(LocalDate dueDate) {
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent dc =
                new ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent();
        dc.setCode(new CodeableConcept().setText(DUE_CRITERION_TEXT));
        dc.setValue(toDate(dueDate));
        return Collections.singletonList(dc);
    }

    default CodeableConcept firstVaccineCode(ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r) {
        if (r == null || !r.hasVaccineCode() || r.getVaccineCode().isEmpty()) return null;
        return r.getVaccineCode().get(0);
    }

    default String extractVaccineCode(ImmunizationRecommendation resource) {
        if (resource == null || !resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r = resource.getRecommendationFirstRep();
        CodeableConcept cc = firstVaccineCode(r);
        if (cc == null) return null;
        for (Coding c : cc.getCoding()) {
            if (CVX_SYSTEM.equals(c.getSystem()) && c.hasCode()) return c.getCode();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractVaccineDisplay(ImmunizationRecommendation resource) {
        if (resource == null || !resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r = resource.getRecommendationFirstRep();
        CodeableConcept cc = firstVaccineCode(r);
        if (cc == null) return null;
        for (Coding c : cc.getCoding()) {
            if (CVX_SYSTEM.equals(c.getSystem()) && c.hasDisplay()) return c.getDisplay();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractStatus(ImmunizationRecommendation resource) {
        if (resource == null || !resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r = resource.getRecommendationFirstRep();
        if (!r.hasForecastStatus()) return null;
        CodeableConcept cs = r.getForecastStatus();
        return cs.hasText() ? cs.getText() : null;
    }

    default LocalDate extractDueDate(ImmunizationRecommendation resource) {
        if (resource == null || !resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent r = resource.getRecommendationFirstRep();
        if (!r.hasDateCriterion() || r.getDateCriterion().isEmpty()) return null;

        for (ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent dc : r.getDateCriterion()) {
            String codeText = dc.hasCode() ? dc.getCode().getText() : null;
            Date v = dc.getValue();
            if (DUE_CRITERION_TEXT.equals(codeText) && v != null) {
                return v.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            }
        }
        return null;
    }

    default LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    default Date toDate(LocalDate date) {
        if (date == null) return null;
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    default Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}