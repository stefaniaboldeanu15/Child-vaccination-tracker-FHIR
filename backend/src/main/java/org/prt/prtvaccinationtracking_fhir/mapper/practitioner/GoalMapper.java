package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.DateType;
import org.hl7.fhir.r5.model.Goal;
import org.hl7.fhir.r5.model.Identifier;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.CreateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.GoalDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.UpdateGoalRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("practitionerGoalMapper")
public class GoalMapper {

    private static final String CARE_PLAN_ID_SYSTEM = "app:care-plan-id";

    private final MapperSupport support;

    public GoalMapper(MapperSupport support) {
        this.support = support;
    }

    public GoalDTO toDTO(Goal resource) {
        if (resource == null) {
            return null;
        }

        return new GoalDTO(
                resource.getIdElement().getIdPart(),
                resource.hasSubject() ? support.referenceToId(resource.getSubject()) : null,
                resource.hasLifecycleStatus() ? resource.getLifecycleStatus().toCode() : null,
                resource.hasDescription() ? support.codeableConceptToText(resource.getDescription()) : null,
                extractTargetDueDate(resource),
                extractStartDate(resource),
                extractCarePlanId(resource)
        );
    }

    public Goal toResource(CreateGoalRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Goal resource = new Goal();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setSubject(support.toPatientReference(dto.patientId()));
        }

        if (dto.lifecycleStatus() != null && !dto.lifecycleStatus().isBlank()) {
            resource.setLifecycleStatus(Goal.GoalLifecycleStatus.fromCode(dto.lifecycleStatus()));
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            resource.setDescription(new CodeableConcept().setText(dto.description()));
        }

        if (dto.startDate() != null) {
            resource.setStart(new DateType(support.toDate(dto.startDate())));
        }

        if (dto.targetDueDate() != null) {
            resource.setTarget(List.of(
                    new Goal.GoalTargetComponent().setDue(new DateType(support.toDate(dto.targetDueDate())))
            ));
        }

        if (dto.carePlanId() != null && !dto.carePlanId().isBlank()) {
            resource.setIdentifier(List.of(
                    new Identifier()
                            .setSystem(CARE_PLAN_ID_SYSTEM)
                            .setValue(dto.carePlanId())
            ));
        }

        return resource;
    }

    public void updateResource(UpdateGoalRequestDTO dto, Goal resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setSubject(support.toPatientReference(dto.patientId()));
        }

        if (dto.lifecycleStatus() != null && !dto.lifecycleStatus().isBlank()) {
            resource.setLifecycleStatus(Goal.GoalLifecycleStatus.fromCode(dto.lifecycleStatus()));
        }

        if (dto.description() != null && !dto.description().isBlank()) {
            resource.setDescription(new CodeableConcept().setText(dto.description()));
        }
    }

    private LocalDate extractTargetDueDate(Goal resource) {
        if (!resource.hasTarget() || resource.getTarget().isEmpty()) {
            return null;
        }

        Goal.GoalTargetComponent target = resource.getTargetFirstRep();
        if (!target.hasDueDateType()) {
            return null;
        }

        return support.toLocalDate(target.getDueDateType().getValue());
    }

    private LocalDate extractStartDate(Goal resource) {
        if (!resource.hasStartDateType()) {
            return null;
        }

        return support.toLocalDate(resource.getStartDateType().getValue());
    }

    private String extractCarePlanId(Goal resource) {
        if (!resource.hasIdentifier() || resource.getIdentifier().isEmpty()) {
            return null;
        }

        for (Identifier identifier : resource.getIdentifier()) {
            if (identifier == null || !identifier.hasValue()) {
                continue;
            }

            if (!identifier.hasSystem() || CARE_PLAN_ID_SYSTEM.equals(identifier.getSystem())) {
                return identifier.getValue();
            }
        }

        return null;
    }
}