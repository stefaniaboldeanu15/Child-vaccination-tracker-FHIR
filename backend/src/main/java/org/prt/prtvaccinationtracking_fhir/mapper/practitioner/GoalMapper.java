package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.Goal;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Reference;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.CreateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.GoalDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.UpdateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface GoalMapper {

    String CAREPLAN_IDENTIFIER_SYSTEM = "app:careplan";

    /// FHIR to DTO
    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId",
            expression = "java(resource.getSubject() != null ? resource.getSubject().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "lifecycleStatus",
            expression = "java(resource.getLifecycleStatus() != null ? resource.getLifecycleStatus().toCode() : null)")
    @Mapping(target = "description",
            expression = "java(resource.getDescription() != null ? resource.getDescription().getText() : null)")
    @Mapping(target = "targetDueDate",
            expression = "java(toLocalDate(extractTargetDueDateType(resource)))")
    @Mapping(target = "startDate",
            expression = "java(toLocalDate(extractStartDateType(resource)))")
    @Mapping(target = "carePlanId",
            expression = "java(extractCarePlanId(resource))")
    GoalDTO toDTO(Goal resource);

    /// DTO to FHIR (Create)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "subject",
            expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "lifecycleStatus",
            expression = "java(dto.lifecycleStatus() != null ? Goal.GoalLifecycleStatus.fromCode(dto.lifecycleStatus()) : null)")
    @Mapping(target = "description",
            expression = "java(toCodeableConcept(dto.description()))")
    @Mapping(target = "target",
            expression = "java(dto.targetDueDate() != null ? toTargetList(dto.targetDueDate()) : Collections.emptyList())")
    @Mapping(target = "start",
            expression = "java(dto.startDate() != null ? new DateType(toDate(dto.startDate())) : null)")
    @Mapping(target = "identifier",
            expression = "java(dto.carePlanId() != null ? toCarePlanIdentifierList(dto.carePlanId()) : Collections.emptyList())")
    Goal toResource(CreateGoalRequestDTO dto);

    /// UPDATE DTO to EXISTING RESOURCE
    @Mapping(target = "subject",
            expression = "java(dto.patientId() != null ? new Reference(\"Patient/\" + dto.patientId()) : resource.getSubject())")
    @Mapping(target = "lifecycleStatus",
            expression = "java(dto.lifecycleStatus() != null ? Goal.GoalLifecycleStatus.fromCode(dto.lifecycleStatus()) : resource.getLifecycleStatus())")
    @Mapping(target = "description",
            expression = "java(dto.description() != null ? toCodeableConcept(dto.description()) : resource.getDescription())")

    // UpdateGoalRequestDTO nu are targetDueDate/startDate/carePlanId => păstrăm ce e pe resource
    @Mapping(target = "target",
            expression = "java(resource.getTarget())")
    @Mapping(target = "start",
            expression = "java(resource.getStart())")
    @Mapping(target = "identifier",
            expression = "java(resource.getIdentifier())")

    @Mapping(target = "id", ignore = true)
    void updateResourceFromDTO(UpdateGoalRequestDTO dto,
                               @MappingTarget Goal resource);

    // -------------------------
    // HELPER METHODS
    // -------------------------

    default CodeableConcept toCodeableConcept(String text) {
        if (text == null) return null;
        CodeableConcept cc = new CodeableConcept();
        cc.setText(text);
        return cc;
    }

    default List<Goal.GoalTargetComponent> toTargetList(LocalDate dueDate) {
        if (dueDate == null) return Collections.emptyList();
        Goal.GoalTargetComponent target = new Goal.GoalTargetComponent();
        target.setDue(new DateType(toDate(dueDate))); // dueDate (date)
        return Collections.singletonList(target);
    }

    default List<Identifier> toCarePlanIdentifierList(String carePlanId) {
        if (carePlanId == null) return Collections.emptyList();
        Identifier id = new Identifier();
        id.setSystem(CAREPLAN_IDENTIFIER_SYSTEM);
        id.setValue(carePlanId);
        return Collections.singletonList(id);
    }

    /**
     * Extracts Goal.target[0].dueDate as DateType (if present).
     */
    default DateType extractTargetDueDateType(Goal resource) {
        if (resource == null || !resource.hasTarget() || resource.getTarget().isEmpty()) return null;
        Goal.GoalTargetComponent t = resource.getTargetFirstRep();
        if (!t.hasDueDateType()) return null;
        return t.getDueDateType();
    }

    /**
     * Extracts Goal.start[x] if it's a DateType (we store date).
     */
    default DateType extractStartDateType(Goal resource) {
        if (resource == null || !resource.hasStart()) return null;
        if (resource.getStart() instanceof DateType dt) return dt;
        return null;
    }

    /**
     * Finds the carePlanId stored as identifier(system=app:careplan).
     */
    default String extractCarePlanId(Goal resource) {
        if (resource == null || !resource.hasIdentifier()) return null;
        for (Identifier id : resource.getIdentifier()) {
            if (CAREPLAN_IDENTIFIER_SYSTEM.equals(id.getSystem())) {
                return id.getValue();
            }
        }
        return null;
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