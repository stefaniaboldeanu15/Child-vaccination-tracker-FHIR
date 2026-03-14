package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Observation;
import org.hl7.fhir.r5.model.Quantity;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.CreateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.UpdateObservationRequestDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component("practitionerObservationMapper")
public class ObservationMapper {

    private final MapperSupport support;

    public ObservationMapper(MapperSupport support) {
        this.support = support;
    }

    public ObservationDTO toDTO(Observation resource) {
        if (resource == null) {
            return null;
        }

        return new ObservationDTO(
                resource.getIdElement().getIdPart(),
                extractCode(resource),
                extractDisplay(resource),
                extractValue(resource),
                extractUnit(resource),
                extractEffectiveDateTime(resource)
        );
    }

    public Observation toResource(CreateObservationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Observation resource = new Observation();

        if (dto.code() != null || dto.display() != null) {
            resource.setCode(toCodeableConcept(dto.code(), dto.display()));
        }

        if (dto.value() != null || dto.unit() != null) {
            resource.setValue(toQuantity(dto.value(), dto.unit()));
        }

        if (dto.effectiveDateTime() != null) {
            resource.setEffective(new DateTimeType(toDate(dto.effectiveDateTime())));
        }

        if (dto.encounterId() != null && !dto.encounterId().isBlank()) {
            resource.setEncounter(support.toEncounterReference(dto.encounterId()));
        }

        resource.setStatus(Enumerations.ObservationStatus.FINAL);
        return resource;
    }

    public void updateResource(UpdateObservationRequestDTO dto, Observation resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.value() != null || dto.unit() != null) {
            resource.setValue(updateQuantity(resource.hasValueQuantity() ? resource.getValueQuantity() : null, dto.value(), dto.unit()));
        }
    }

    private org.hl7.fhir.r5.model.CodeableConcept toCodeableConcept(String code, String display) {
        org.hl7.fhir.r5.model.CodeableConcept concept = new org.hl7.fhir.r5.model.CodeableConcept();

        if (display != null && !display.isBlank()) {
            concept.setText(display);
        }

        if (code != null && !code.isBlank()) {
            concept.addCoding(new Coding().setCode(code).setDisplay(display));
        }

        return concept;
    }

    private Quantity toQuantity(String value, String unit) {
        Quantity quantity = new Quantity();

        if (value != null && !value.isBlank()) {
            try {
                quantity.setValue(Double.parseDouble(value));
            } catch (NumberFormatException ignored) {
                quantity.setValueElement(null);
            }
        }

        if (unit != null && !unit.isBlank()) {
            quantity.setUnit(unit);
        }

        return quantity;
    }

    private Quantity updateQuantity(Quantity existing, String value, String unit) {
        Quantity quantity = existing != null ? existing : new Quantity();

        if (value != null) {
            if (value.isBlank()) {
                quantity.setValueElement(null);
            } else {
                try {
                    quantity.setValue(Double.parseDouble(value));
                } catch (NumberFormatException ignored) {
                    quantity.setValueElement(null);
                }
            }
        }

        if (unit != null) {
            if (unit.isBlank()) {
                quantity.setUnit(null);
            } else {
                quantity.setUnit(unit);
            }
        }

        return quantity;
    }

    private String extractCode(Observation resource) {
        if (!resource.hasCode() || !resource.getCode().hasCoding()) {
            return null;
        }

        Coding coding = resource.getCode().getCodingFirstRep();
        return coding.hasCode() ? coding.getCode() : null;
    }

    private String extractDisplay(Observation resource) {
        if (!resource.hasCode()) {
            return null;
        }

        if (resource.getCode().hasCoding() && resource.getCode().getCodingFirstRep().hasDisplay()) {
            return resource.getCode().getCodingFirstRep().getDisplay();
        }

        return resource.getCode().hasText() ? resource.getCode().getText() : null;
    }

    private String extractValue(Observation resource) {
        if (!resource.hasValueQuantity()) {
            return null;
        }

        Quantity quantity = resource.getValueQuantity();
        return quantity.hasValue() ? quantity.getValue().toPlainString() : null;
    }

    private String extractUnit(Observation resource) {
        if (!resource.hasValueQuantity()) {
            return null;
        }

        Quantity quantity = resource.getValueQuantity();
        return quantity.hasUnit() ? quantity.getUnit() : null;
    }

    private LocalDateTime extractEffectiveDateTime(Observation resource) {
        if (!resource.hasEffectiveDateTimeType()) {
            return null;
        }

        return toLocalDateTime(resource.getEffectiveDateTimeType().getValue());
    }

    private Date toDate(LocalDateTime value) {
        if (value == null) {
            return null;
        }

        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    private LocalDateTime toLocalDateTime(Date value) {
        if (value == null) {
            return null;
        }

        return Instant.ofEpochMilli(value.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}