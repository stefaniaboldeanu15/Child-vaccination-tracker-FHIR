package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface ObservationMapper {

    String LOINC_SYSTEM = "http://loinc.org";

    @Mapping(target = "observationId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "immunizationId", expression = "java(resource.getEncounter() != null ? resource.getEncounter().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "code", expression = "java(extractCode(resource))")
    @Mapping(target = "display", expression = "java(extractDisplay(resource))")
    @Mapping(target = "value", expression = "java(extractValue(resource))")
    @Mapping(target = "unit", expression = "java(extractUnit(resource))")
    @Mapping(target = "effectiveDateTime", expression = "java(resource.hasEffective() ? resource.getEffective().primitiveValue() : null)")
    ObservationDTO toDTO(Observation resource);

    default String extractCode(Observation resource) {
        if (resource == null || !resource.hasCode()) return null;
        CodeableConcept cc = resource.getCode();
        for (Coding c : cc.getCoding()) {
            if (LOINC_SYSTEM.equals(c.getSystem()) && c.hasCode()) return c.getCode();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractDisplay(Observation resource) {
        if (resource == null || !resource.hasCode()) return null;
        CodeableConcept cc = resource.getCode();
        for (Coding c : cc.getCoding()) {
            if (LOINC_SYSTEM.equals(c.getSystem()) && c.hasDisplay()) return c.getDisplay();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractValue(Observation resource) {
        if (resource == null || !resource.hasValue()) return null;
        if (resource.getValue() instanceof Quantity q && q.hasValue()) return q.getValue().toPlainString();
        if (resource.getValue() instanceof StringType s && s.hasValue()) return s.getValue();
        if (resource.getValue() instanceof CodeableConcept cc && cc.hasText()) return cc.getText();
        return null;
    }

    default String extractUnit(Observation resource) {
        if (resource == null || !resource.hasValue()) return null;
        if (resource.getValue() instanceof Quantity q && q.hasUnit()) return q.getUnit();
        return null;
    }
}


