package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Annotation;
import org.hl7.fhir.r5.model.CarePlan;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.Reference;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.CarePlanDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.CreateCarePlanRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.UpdateCarePlanRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class CarePlanMapper {

    private final MapperSupport support;

    public CarePlanMapper(MapperSupport support) {
        this.support = support;
    }

    public CarePlanDTO toDTO(CarePlan resource) {
        if (resource == null) {
            return null;
        }

        return new CarePlanDTO(
                resource.getIdElement().getIdPart(),
                resource.hasSubject() ? support.referenceToId(resource.getSubject()) : null,
                resource.hasTitle() ? resource.getTitle() : null,
                extractNote(resource),
                extractStartDate(resource),
                extractEndDate(resource),
                resource.hasStatus() ? resource.getStatus().toCode() : null,
                resource.hasIntent() ? resource.getIntent().toCode() : null,
                extractGoalIds(resource)
        );
    }

    public CarePlan toResource(CreateCarePlanRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        CarePlan resource = new CarePlan();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setSubject(support.toPatientReference(dto.patientId()));
        }

        if (dto.title() != null) {
            resource.setTitle(dto.title());
        }

        if (dto.note() != null && !dto.note().isBlank()) {
            resource.setNote(List.of(new Annotation().setText(dto.note())));
        }

        if (dto.startDate() != null || dto.endDate() != null) {
            Period period = new Period();

            if (dto.startDate() != null) {
                period.setStart(support.toDate(dto.startDate()));
            }

            if (dto.endDate() != null) {
                period.setEnd(support.toDate(dto.endDate()));
            }

            resource.setPeriod(period);
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Enumerations.RequestStatus.fromCode(dto.status()));
        }

        resource.setIntent(CarePlan.CarePlanIntent.PLAN);
        return resource;
    }

    public void updateResource(UpdateCarePlanRequestDTO dto, CarePlan resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.title() != null) {
            resource.setTitle(dto.title());
        }

        if (dto.description() != null) {
            if (dto.description().isBlank()) {
                resource.setNote(new ArrayList<>());
            } else {
                resource.setNote(List.of(new Annotation().setText(dto.description())));
            }
        }

        if (dto.endDate() != null) {
            Period period = resource.hasPeriod() ? resource.getPeriod() : new Period();
            period.setEnd(support.toDate(dto.endDate()));
            resource.setPeriod(period);
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Enumerations.RequestStatus.fromCode(dto.status()));
        }
    }

    private String extractNote(CarePlan resource) {
        if (!resource.hasNote() || resource.getNote().isEmpty()) {
            return null;
        }

        Annotation note = resource.getNoteFirstRep();
        return note.hasText() ? note.getText() : null;
    }

    private LocalDate extractStartDate(CarePlan resource) {
        if (!resource.hasPeriod() || !resource.getPeriod().hasStart()) {
            return null;
        }
        return support.toLocalDate(resource.getPeriod().getStart());
    }

    private LocalDate extractEndDate(CarePlan resource) {
        if (!resource.hasPeriod() || !resource.getPeriod().hasEnd()) {
            return null;
        }
        return support.toLocalDate(resource.getPeriod().getEnd());
    }

    private List<String> extractGoalIds(CarePlan resource) {
        List<String> goalIds = new ArrayList<>();

        if (!resource.hasGoal()) {
            return goalIds;
        }

        for (Reference goal : resource.getGoal()) {
            String id = support.referenceToId(goal);
            if (id != null) {
                goalIds.add(id);
            }
        }

        return goalIds;
    }
}