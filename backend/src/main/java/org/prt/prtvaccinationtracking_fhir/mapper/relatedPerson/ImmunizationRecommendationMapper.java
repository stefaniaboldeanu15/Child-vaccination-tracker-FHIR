package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.ImmunizationRecommendationDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper(config = BaseMapperConfig.class)
public interface ImmunizationRecommendationMapper {

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getPatient() != null ? resource.getPatient().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "vaccineCode", expression = "java(extractVaccineCode(resource))")
    @Mapping(target = "vaccineDisplay", expression = "java(extractVaccineDisplay(resource))")
    @Mapping(target = "dueDate", expression = "java(extractDueDate(resource))")
    @Mapping(target = "status", expression = "java(extractStatus(resource))")
    @Mapping(target = "series", expression = "java(extractSeries(resource))")
    @Mapping(target = "doseNumber", expression = "java(extractDoseNumber(resource))")
    ImmunizationRecommendationDTO toDTO(ImmunizationRecommendation resource);

    default String extractVaccineCode(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent rec = resource.getRecommendationFirstRep();
        if (!rec.hasVaccineCode() || rec.getVaccineCode().isEmpty()) return null;
        CodeableConcept cc = rec.getVaccineCodeFirstRep();
        if (cc.hasCoding() && !cc.getCoding().isEmpty()) {
            Coding c = cc.getCodingFirstRep();
            if (c.hasCode()) return c.getCode();
        }
        return cc.getText();
    }

    default String extractVaccineDisplay(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent rec = resource.getRecommendationFirstRep();
        if (!rec.hasVaccineCode() || rec.getVaccineCode().isEmpty()) return null;
        CodeableConcept cc = rec.getVaccineCodeFirstRep();
        if (cc.hasCoding() && !cc.getCoding().isEmpty()) {
            Coding c = cc.getCodingFirstRep();
            if (c.hasDisplay()) return c.getDisplay();
        }
        return cc.getText();
    }

    default LocalDate extractDueDate(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent rec = resource.getRecommendationFirstRep();
        if (!rec.hasDateCriterion()) return null;
        for (ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent dc : rec.getDateCriterion()) {
            if (dc.hasValue()) {
                Date d = dc.getValue().getValue();
                if (d != null) {
                    return d.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                }
            }
        }
        return null;
    }

    default String extractStatus(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent rec = resource.getRecommendationFirstRep();
        return rec.hasForecastStatus() ? rec.getForecastStatus().getText() : null;
    }

    default String extractSeries(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent rec = resource.getRecommendationFirstRep();
        return rec.hasSeries() ? rec.getSeries() : null;
    }

    default Integer extractDoseNumber(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation() || resource.getRecommendation().isEmpty()) return null;
        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent rec = resource.getRecommendationFirstRep();
        if (!rec.hasDoseNumber()) return null;
        try {
            return Integer.parseInt(rec.getDoseNumberStringType().getValue());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}


