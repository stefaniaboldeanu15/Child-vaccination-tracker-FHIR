package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.AdverseEvent;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Reference;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.adverseEvent.AdverseEventDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.adverseEvent.CreateAdverseEventRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.adverseEvent.UpdateAdverseEventRequestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("relatedpersonAdverseEventMapper")
public class AdverseEventMapper {

    private final MapperSupport support;

    public AdverseEventMapper(MapperSupport support) {
        this.support = support;
    }

    public AdverseEventDTO toDTO(AdverseEvent resource) {
        if (resource == null) {
            return null;
        }

        return new AdverseEventDTO(
                resource.getIdElement().getIdPart(),
                resource.hasStatus() ? resource.getStatus() : null,
                resource.hasActuality() ? resource.getActuality() : null,
                extractCategory(resource),
                resource.hasRecordedDate() ? support.toLocalDate(resource.getRecordedDate()) : null,
                resource.hasEncounter() ? support.referenceToId(resource.getEncounter()) : null
        );
    }

    public AdverseEvent toResource(CreateAdverseEventRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        AdverseEvent resource = new AdverseEvent();

        if (dto.status() != null) {
            resource.setStatus(mapStatus(dto.status()));
        }

        if (dto.actuality() != null) {
            resource.setActuality(mapActuality(dto.actuality()));
        }

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setSubject(support.toPatientReference(dto.patientId()));
        }

        if (dto.category() != null && !dto.category().isBlank()) {
            resource.setCategory(List.of(new CodeableConcept().setText(dto.category())));
        }

        if (dto.recordedDate() != null) {
            resource.setRecordedDate(support.toDate(dto.recordedDate()));
        }

        if (dto.encounter() != null && !dto.encounter().isBlank()) {
            resource.setEncounter(support.toEncounterReference(dto.encounter()));
        }

        return resource;
    }

    public void updateResource(UpdateAdverseEventRequestDTO dto, AdverseEvent resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.status() != null) {
            resource.setStatus(mapStatus(dto.status()));
        }

        if (dto.actuality() != null) {
            resource.setActuality(mapActuality(dto.actuality()));
        }

        if (dto.category() != null) {
            if (dto.category().isBlank()) {
                resource.setCategory(List.of());
            } else {
                resource.setCategory(List.of(new CodeableConcept().setText(dto.category())));
            }
        }

        if (dto.recordedDate() != null) {
            resource.setRecordedDate(support.toDate(dto.recordedDate()));
        }

        if (dto.encounter() != null) {
            if (dto.encounter().isBlank()) {
                resource.setEncounter((Reference) null);
            } else {
                resource.setEncounter(support.toEncounterReference(dto.encounter()));
            }
        }
    }

    private String extractCategory(AdverseEvent resource) {
        if (!resource.hasCategory() || resource.getCategory().isEmpty()) {
            return null;
        }
        return support.codeableConceptToText(resource.getCategoryFirstRep());
    }

    private AdverseEvent.AdverseEventStatus mapStatus(AdverseEventDTO.AdverseEventStatus status) {
        if (status == null) {
            return null;
        }

        return switch (status) {
            case IN_PROGRESS -> AdverseEvent.AdverseEventStatus.INPROGRESS;
            case COMPLETED -> AdverseEvent.AdverseEventStatus.COMPLETED;
            case ENTERED_IN_ERROR -> AdverseEvent.AdverseEventStatus.ENTEREDINERROR;
            case UNKNOWN -> AdverseEvent.AdverseEventStatus.UNKNOWN;
        };
    }

    private AdverseEvent.AdverseEventActuality mapActuality(AdverseEventDTO.AdverseEventActuality actuality) {
        if (actuality == null) {
            return null;
        }

        return switch (actuality) {
            case ACTUAL -> AdverseEvent.AdverseEventActuality.ACTUAL;
            case POTENTIAL -> AdverseEvent.AdverseEventActuality.POTENTIAL;
        };
    }
}