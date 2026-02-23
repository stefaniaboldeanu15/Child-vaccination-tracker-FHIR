package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.AdverseEvent;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.DateType;

import org.mapstruct.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.AdverseEventDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.CreateAdverseEventRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.UpdateAdverseEventRequestDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper
        (config = BaseMapperConfig.class)
public interface AdverseEventMapper {

    /// FHIR to DTO
    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "status",
            expression = "java(mapStatus(resource.getStatus()))")
    @Mapping(target = "actuality",
            expression = "java(mapActuality(resource.getActuality()))")
    @Mapping(target = "category",
            expression = "java(resource.hasCategory() ? resource.getCategoryFirstRep().getText() : null)")
    @Mapping(target = "recordedDate",
            expression = "java(toLocalDate(resource.getRecordedDateElement()))")
    @Mapping(target = "encounter",
            expression = "java(resource.getEncounter() != null ? resource.getEncounter().getReferenceElement().getIdPart() : null)")
    AdverseEventDTO toDTO(AdverseEvent resource);

    ///DTO to FHIR
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status",
            expression = "java(mapStatus(dto.status()))")
    @Mapping(target = "actuality",
            expression = "java(mapActuality(dto.actuality()))")
    @Mapping(target = "subject",
            expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "category",
            expression = "java(toCategoryList(dto.category()))")
    @Mapping(target = "recordedDate",
            expression = "java(toDate(dto.recordedDate()))")
    @Mapping(target = "encounter",
            expression = "java(dto.encounter() != null ? new Reference(\"Encounter/\" + dto.encounter()) : null)")
    AdverseEvent toResource(CreateAdverseEventRequestDTO dto);

    /// UPDATE DTO to EXISTING RESOURCE

    @Mapping(target = "status",
            expression = "java(dto.status() != null ? mapStatus(dto.status()) : resource.getStatus())")
    @Mapping(target = "actuality",
            expression = "java(dto.actuality() != null ? mapActuality(dto.actuality()) : resource.getActuality())")
    @Mapping(target = "category",
            expression = "java(dto.category() != null ? toCategoryList(dto.category()) : resource.getCategory())")
    @Mapping(target = "recordedDate",
            expression = "java(dto.recordedDate() != null ? toDate(dto.recordedDate()) : resource.getRecordedDate())")
    @Mapping(target = "encounter",
            expression = "java(dto.encounter() != null ? new Reference(\"Encounter/\" + dto.encounter()) : resource.getEncounter())")
    void updateResourceFromDTO(UpdateAdverseEventRequestDTO dto,
                               @MappingTarget AdverseEvent resource);

    /// HELPER METHODS

    default AdverseEventDTO.AdverseEventStatus mapStatus(AdverseEvent.AdverseEventStatus status) {
        if (status == null) return null;
        return AdverseEventDTO.AdverseEventStatus.valueOf(status.name());
    }

    default AdverseEvent.AdverseEventStatus mapStatus(AdverseEventDTO.AdverseEventStatus status) {
        if (status == null) return null;
        return AdverseEvent.AdverseEventStatus.valueOf(status.name());
    }

    default AdverseEventDTO.AdverseEventActuality mapActuality(AdverseEvent.AdverseEventActuality actuality) {
        if (actuality == null) return null;
        return AdverseEventDTO.AdverseEventActuality.valueOf(actuality.name());
    }

    default AdverseEvent.AdverseEventActuality mapActuality(AdverseEventDTO.AdverseEventActuality actuality) {
        if (actuality == null) return null;
        return AdverseEvent.AdverseEventActuality.valueOf(actuality.name());
    }

    default java.util.List<CodeableConcept> toCategoryList(String text) {
        if (text == null) return null;
        CodeableConcept concept = new CodeableConcept();
        concept.setText(text);
        return java.util.Collections.singletonList(concept);
    }

    default LocalDate toLocalDate(DateType type) {
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
