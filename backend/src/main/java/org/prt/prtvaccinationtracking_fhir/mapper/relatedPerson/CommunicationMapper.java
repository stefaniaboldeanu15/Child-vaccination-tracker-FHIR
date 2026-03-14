package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.CommunicationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.CreateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.UpdateCommunicationRequestDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component("relatedpersonCommunicationMapper")
public class CommunicationMapper {

    private final MapperSupport support;

    public CommunicationMapper(MapperSupport support) {
        this.support = support;
    }

    public CommunicationDTO toDTO(Communication resource) {
        if (resource == null) {
            return null;
        }

        return new CommunicationDTO(
                resource.getIdElement().getIdPart(),
                extractPatientId(resource),
                extractRelatedPersonId(resource),
                extractRecommendationId(resource),
                extractMedium(resource),
                extractMessage(resource),
                resource.hasStatus() ? resource.getStatus().toCode() : null,
                toLocalDateTime(resource.hasSent() ? resource.getSent() : null),
                toLocalDateTime(resource.hasReceived() ? resource.getReceived() : null)
        );
    }

    public Communication toResource(CreateCommunicationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Communication resource = new Communication();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setSubject(support.toPatientReference(dto.patientId()));
        }

        if (dto.relatedPersonId() != null && !dto.relatedPersonId().isBlank()) {
            resource.setRecipient(List.of(support.toRelatedPersonReference(dto.relatedPersonId())));
        }

        if (dto.recommendationId() != null && !dto.recommendationId().isBlank()) {
            resource.setAbout(List.of(new Reference("ImmunizationRecommendation/" + dto.recommendationId())));
        }

        if (dto.medium() != null && !dto.medium().isBlank()) {
            resource.setMedium(List.of(new CodeableConcept().setText(dto.medium())));
        }

        if (dto.message() != null && !dto.message().isBlank()) {
            Communication.CommunicationPayloadComponent payload =
                    new Communication.CommunicationPayloadComponent();
            payload.setContent(new StringType(dto.message()));
            resource.setPayload(List.of(payload));
        }

        if (dto.sentDate() != null) {
            resource.setSent(toDate(dto.sentDate()));
        }

        resource.setStatus(Enumerations.EventStatus.COMPLETED);
        return resource;
    }

    public void updateResource(UpdateCommunicationRequestDTO dto, Communication resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Enumerations.EventStatus.fromCode(dto.status()));
        }
    }

    private String extractPatientId(Communication resource) {
        if (!resource.hasSubject()) {
            return null;
        }
        return support.referenceToId(resource.getSubject());
    }

    private String extractRelatedPersonId(Communication resource) {
        if (!resource.hasRecipient() || resource.getRecipient().isEmpty()) {
            return null;
        }

        for (Reference recipient : resource.getRecipient()) {
            if (recipient != null && recipient.hasReference() && recipient.getReference().startsWith("RelatedPerson/")) {
                return support.referenceToId(recipient);
            }
        }

        return null;
    }

    private String extractRecommendationId(Communication resource) {
        if (!resource.hasAbout() || resource.getAbout().isEmpty()) {
            return null;
        }

        for (Reference about : resource.getAbout()) {
            if (about != null
                    && about.hasReference()
                    && about.getReference().startsWith("ImmunizationRecommendation/")) {
                return support.referenceToId(about);
            }
        }

        return null;
    }

    private String extractMedium(Communication resource) {
        if (!resource.hasMedium() || resource.getMedium().isEmpty()) {
            return null;
        }
        return support.codeableConceptToText(resource.getMediumFirstRep());
    }

    private String extractMessage(Communication resource) {
        if (!resource.hasPayload() || resource.getPayload().isEmpty()) {
            return null;
        }

        Communication.CommunicationPayloadComponent payload = resource.getPayloadFirstRep();
        if (payload == null || !payload.hasContent()) {
            return null;
        }

        if (payload.getContent() instanceof StringType stringType) {
            return stringType.getValue();
        }

        if (payload.getContent() instanceof Reference reference) {
            return support.referenceToId(reference);
        }

        if (payload.getContent() instanceof CodeableConcept concept) {
            return support.codeableConceptToText(concept);
        }

        if (payload.getContent() instanceof CodeableReference codeableReference) {
            if (codeableReference.hasConcept()) {
                return support.codeableConceptToText(codeableReference.getConcept());
            }
            if (codeableReference.hasReference()) {
                return support.referenceToId(codeableReference.getReference());
            }
        }

        return payload.getContent().primitiveValue();
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