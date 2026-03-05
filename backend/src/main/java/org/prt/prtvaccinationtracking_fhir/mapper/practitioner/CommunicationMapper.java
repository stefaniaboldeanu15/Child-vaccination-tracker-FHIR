package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Mapper(config = BaseMapperConfig.class)
public interface CommunicationMapper {

     ///FHIR → DTO

    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId",
            expression = "java(resource.getSubject() != null ? resource.getSubject().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "relatedPersonId",
            expression = "java(extractRecipient(resource))")
    @Mapping(target = "recommendationId",
            expression = "java(resource.hasBasedOn() ? resource.getBasedOnFirstRep().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "medium",
            expression = "java(resource.hasMedium() ? resource.getMediumFirstRep().getText() : null)")
    @Mapping(target = "message",
            expression = "java(resource.hasPayload() ? resource.getPayloadFirstRep().getContentStringType().getValue() : null)")
    @Mapping(target = "status",
            expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    @Mapping(target = "sentDate",
            expression = "java(toLocalDateTime(resource.getSentElement()))")
    @Mapping(target = "receivedDate",
            expression = "java(toLocalDateTime(resource.getReceivedElement()))")

    CommunicationDTO toDTO(Communication resource);

     /// CREATE DTO → FHIR

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status",
            expression = "java(org.hl7.fhir.r5.model.Enumerations.CommunicationStatus.COMPLETED)")
    @Mapping(target = "subject",
            expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "recipient",
            expression = "java(buildRecipient(dto.relatedPersonId()))")
    @Mapping(target = "basedOn",
            expression = "java(dto.recommendationId() != null ? List.of(new Reference(\"ImmunizationRecommendation/\" + dto.recommendationId())) : null)")
    @Mapping(target = "medium",
            expression = "java(dto.medium() != null ? List.of(toCodeableConcept(dto.medium())) : null)")
    @Mapping(target = "payload",
            expression = "java(buildPayload(dto.message()))")
    @Mapping(target = "sent",
            expression = "java(toDate(dto.sentDate()))")

    Communication toResource(CreateCommunicationRequestDTO dto);

     /// UPDATE DTO → EXISTING RESOURCE

    @Mapping(target = "status",
            expression = "java(dto.status() != null ? org.hl7.fhir.r5.model.Enumerations.CommunicationStatus.fromCode(dto.status()) : resource.getStatus())")
    void updateResourceFromDTO(UpdateCommunicationRequestDTO dto,
                               @MappingTarget Communication resource);

    /// HELPERS

    default String extractRecipient(Communication resource) {
        if (resource.getRecipient() == null) return null;

        return resource.getRecipient().stream()
                .filter(r -> r.getReference() != null && r.getReference().startsWith("RelatedPerson"))
                .map(r -> r.getReferenceElement().getIdPart())
                .findFirst()
                .orElse(null);
    }

    default List<Reference> buildRecipient(String relatedPersonId) {
        if (relatedPersonId == null) return null;
        return List.of(new Reference("RelatedPerson/" + relatedPersonId));
    }

    default List<Communication.CommunicationPayloadComponent> buildPayload(String message) {
        if (message == null) return null;

        Communication.CommunicationPayloadComponent payload =
                new Communication.CommunicationPayloadComponent();

        payload.setContent(new StringType(message));

        return List.of(payload);
    }

    default CodeableConcept toCodeableConcept(String text) {
        CodeableConcept concept = new CodeableConcept();
        concept.setText(text);
        return concept;
    }

    default LocalDateTime toLocalDateTime(DateTimeType dateTime) {
        if (dateTime == null || dateTime.getValue() == null) return null;

        return dateTime.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    default Date toDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;

        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }
}