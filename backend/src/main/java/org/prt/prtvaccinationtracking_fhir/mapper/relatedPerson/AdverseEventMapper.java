package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.AdverseEventDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface AdverseEventMapper {

    @Mapping(target = "adverseEventId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getSubject() != null ? resource.getSubject().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "encounterId", expression = "java(resource.getEncounter() != null ? resource.getEncounter().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "immunizationId", expression = "java(extractPrimaryImmunizationId(resource))")
    @Mapping(target = "category", expression = "java(resource.hasCategory() ? resource.getCategoryFirstRep().getText() : null)")
    @Mapping(target = "severity", expression = "java(resource.hasSeverity() && resource.getSeverity().hasText() ? resource.getSeverity().getText() : null)")
    @Mapping(target = "outcome", expression = "java(resource.hasOutcome() && resource.getOutcome().hasText() ? resource.getOutcome().getText() : null)")
    @Mapping(target = "date", expression = "java(toLocalDateTime(resource.getDateElement()))")
    @Mapping(target = "code", expression = "java(resource.hasEvent() && resource.getEvent().hasCoding() && !resource.getEvent().getCoding().isEmpty() ? resource.getEvent().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "display", expression = "java(resource.hasEvent() ? resource.getEvent().getText() : null)")
    @Mapping(target = "description", expression = "java(resource.hasDescription() ? resource.getDescription() : null)")
    @Mapping(target = "suspectImmunizationIds", expression = "java(extractSuspectImmunizationIds(resource))")
    AdverseEventDTO toDTO(AdverseEvent resource);

    default String extractPrimaryImmunizationId(AdverseEvent resource) {
        if (resource == null || !resource.hasSuspectEntity()) return null;
        for (AdverseEvent.AdverseEventSuspectEntityComponent comp : resource.getSuspectEntity()) {
            if (!comp.hasInstance()) continue;
            DataType instance = comp.getInstance();
            if (instance instanceof Reference ref
                    && ref.getReference() != null
                    && ref.getReference().startsWith("Immunization/")) {
                return ref.getReferenceElement().getIdPart();
            }
        }
        return null;
    }

    default List<String> extractSuspectImmunizationIds(AdverseEvent resource) {
        List<String> result = new ArrayList<>();
        if (resource == null || !resource.hasSuspectEntity()) return result;
        for (AdverseEvent.AdverseEventSuspectEntityComponent comp : resource.getSuspectEntity()) {
            if (!comp.hasInstance()) continue;
            DataType instance = comp.getInstance();
            if (instance instanceof Reference ref
                    && ref.getReference() != null
                    && ref.getReference().startsWith("Immunization/")) {
                result.add(ref.getReferenceElement().getIdPart());
            }
        }
        return result;
    }

    default LocalDateTime toLocalDateTime(DateTimeType type) {
        if (type == null || type.getValue() == null) return null;
        return type.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }
}


