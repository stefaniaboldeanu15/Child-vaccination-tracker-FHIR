package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = BaseMapperConfig.class)
public interface CarePlanMapper {
    /// FHIR TO DTOs

    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId",
            expression = "java(resource.getSubject().getReferenceElement().getIdPart())")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "note",
            expression = "java(resource.hasNote() ? resource.getNoteFirstRep().getText() : null)")
    @Mapping(target = "startDate",
            expression = "java(resource.hasPeriod() ? toLocalDate(resource.getPeriod().getStartElement()) : null)")
    @Mapping(target = "endDate",
            expression = "java(resource.hasPeriod() ? toLocalDate(resource.getPeriod().getEndElement()) : null)")
    @Mapping(target = "status",
            expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    @Mapping(target = "intent",
            expression = "java(resource.getIntent() != null ? resource.getIntent().toCode() : null)")
    @Mapping(target = "goalIds",
            expression = "java(mapGoals(resource.getGoal()))")

    CarePlanDTO toDTO(CarePlan resource);

    ///CREATE DTO → FHIR

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status",
            expression = "java(org.hl7.fhir.r5.model.Enumerations.CarePlanStatus.fromCode(dto.status()))")
    @Mapping(target = "subject",
            expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "title", source = "title")
    @Mapping(target = "note",
            expression = "java(dto.note() != null ? java.util.List.of(buildAnnotation(dto.note())) : null)")
    @Mapping(target = "period",
            expression = "java(buildPeriod(dto.startDate(), dto.endDate()))")
    @Mapping(target = "intent",
            expression = "java(org.hl7.fhir.r5.model.Enumerations.CarePlanIntent.fromCode(dto.intent()))")
    @Mapping(target = "goal",
            expression = "java(buildGoalReferences(dto.goalIds()))")

    CarePlan toResource(CarePlanDTO dto);

    ///UPDATE DTO → EXISTING RESOURCE

    @Mapping(target = "title",
            expression = "java(dto.title() != null ? dto.title() : resource.getTitle())")
    @Mapping(target = "note",
            expression = "java(dto.note() != null ? java.util.List.of(buildAnnotation(dto.note())) : resource.getNote())")
    @Mapping(target = "period",
            expression = "java(buildPeriod(dto.startDate(), dto.endDate()) != null ? buildPeriod(dto.startDate(), dto.endDate()) : resource.getPeriod())")
    @Mapping(target = "status",
            expression = "java(dto.status() != null ? org.hl7.fhir.r5.model.Enumerations.CarePlanStatus.fromCode(dto.status()) : resource.getStatus())")
    @Mapping(target = "intent",
            expression = "java(dto.intent() != null ? org.hl7.fhir.r5.model.Enumerations.CarePlanIntent.fromCode(dto.intent()) : resource.getIntent())")
    @Mapping(target = "goal",
            expression = "java(dto.goalIds() != null ? buildGoalReferences(dto.goalIds()) : resource.getGoal())")

    void updateResourceFromDTO(CarePlanDTO dto,
                               @MappingTarget CarePlan resource);
    /// helpers

    default LocalDate toLocalDate(DateTimeType dateTime) {
        if (dateTime == null || dateTime.getValue() == null) return null;

        return dateTime.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    default Period buildPeriod(LocalDate start, LocalDate end) {
        if (start == null && end == null) return null;
        Period period = new Period();
        if (start != null) {
            period.setStart(Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        if (end != null) {
            period.setEnd(Date.from(end.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        }
        return period;
    }

    default Annotation buildAnnotation(String text) {
        Annotation annotation = new Annotation();
        annotation.setText(text);
        return annotation;
    }

    default List<String> mapGoals(List<Reference> goals) {
        if (goals == null) return null;

        return goals.stream()
                .map(ref -> ref.getReferenceElement().getIdPart())
                .collect(Collectors.toList());
    }

    default List<Reference> buildGoalReferences(List<String> goalIds) {
        if (goalIds == null) return null;

        return goalIds.stream()
                .map(id -> new Reference("Goal/" + id))
                .collect(Collectors.toList());
    }
}