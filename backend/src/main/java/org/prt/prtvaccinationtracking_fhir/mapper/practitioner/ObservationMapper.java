package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.CreateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.UpdateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Mapper(config = BaseMapperConfig.class)
public interface ObservationMapper {

    String LOINC_SYSTEM = "http://loinc.org";

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "code", expression = "java(extractCode(resource))")
    @Mapping(target = "display", expression = "java(extractDisplay(resource))")
    @Mapping(target = "value", expression = "java(extractValue(resource))")
    @Mapping(target = "unit", expression = "java(extractUnit(resource))")
    @Mapping(target = "effectiveDateTime", expression = "java(toLocalDateTime(extractEffectiveDateTime(resource)))")
    ObservationDTO toDTO(Observation resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(Observation.ObservationStatus.FINAL)")
    @Mapping(target = "code", expression = "java(toCode(dto.code(), dto.display()))")
    @Mapping(target = "value", expression = "java(toQuantity(dto.value(), dto.unit()))")
    @Mapping(target = "effective", expression = "java(dto.effectiveDateTime() != null ? new DateTimeType(toDate(dto.effectiveDateTime())) : null)")
    @Mapping(target = "encounter", expression = "java(dto.encounterId() != null ? new Reference(\"Encounter/\" + dto.encounterId()) : null)")
    Observation toResource(CreateObservationRequestDTO dto);

    @Mapping(target = "value", expression = "java(dto.value() != null || dto.unit() != null ? toQuantity(dto.value() != null ? dto.value() : extractValue(resource), dto.unit() != null ? dto.unit() : extractUnit(resource)) : resource.getValue())")
    void updateResourceFromDTO(UpdateObservationRequestDTO dto, @MappingTarget Observation resource);

    default CodeableConcept toCode(String code, String display) {
        CodeableConcept cc = new CodeableConcept();
        if (code != null) cc.addCoding(new Coding().setSystem(LOINC_SYSTEM).setCode(code).setDisplay(display));
        if (display != null) cc.setText(display);
        return cc;
    }

    default Quantity toQuantity(String value, String unit) {
        Quantity q = new Quantity();
        if (unit != null) q.setUnit(unit);
        if (value != null) {
            try {
                q.setValue(new java.math.BigDecimal(value));
            } catch (NumberFormatException e) {
                q.setValueElement(null);
            }
        }
        return q;
    }

    default DateTimeType extractEffectiveDateTime(Observation resource) {
        if (resource == null || !resource.hasEffective()) return null;
        if (resource.getEffective() instanceof DateTimeType dt) return dt;
        return null;
    }

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

    default LocalDateTime toLocalDateTime(DateTimeType type) {
        if (type == null || type.getValue() == null) return null;
        return type.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    default Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}