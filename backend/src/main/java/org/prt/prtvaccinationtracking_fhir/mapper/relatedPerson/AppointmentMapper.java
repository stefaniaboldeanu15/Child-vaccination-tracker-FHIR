package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.AppointmentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.CreateAppointmentRequest;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface AppointmentMapper {

    // FHIR → DTO (RelatedPerson portal view)
    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(extractParticipantId(resource, \"Patient\"))")
    @Mapping(target = "patientName", expression = "java(extractParticipantName(resource, \"Patient\"))")
    @Mapping(target = "relatedPersonId", expression = "java(extractParticipantId(resource, \"RelatedPerson\"))")
    @Mapping(target = "relatedPersonName", expression = "java(extractParticipantName(resource, \"RelatedPerson\"))")
    @Mapping(target = "start", expression = "java(toLocalDateTime(resource.getStartElement()))")
    @Mapping(target = "end", expression = "java(toLocalDateTime(resource.getEndElement()))")
    @Mapping(target = "status", expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    @Mapping(target = "location", expression = "java(resource.hasDescription() ? resource.getDescription() : null)")
    @Mapping(target = "reason", expression = "java(resource.hasReason() ? resource.getReasonFirstRep().getText() : null)")
    AppointmentDTO toDTO(Appointment resource);

    // CREATE DTO → FHIR
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(org.hl7.fhir.r5.model.Enumerations.AppointmentStatus.BOOKED)")
    @Mapping(target = "description", source = "reason")
    @Mapping(target = "start", expression = "java(toDate(dto.getStart()))")
    @Mapping(target = "end", expression = "java(toDate(dto.getEnd()))")
    @Mapping(target = "participant", expression = "java(buildParticipants(dto.getPatientId()))")
    Appointment toResource(CreateAppointmentRequest dto);

    // Helpers

    default String extractParticipantId(Appointment resource, String typePrefix) {
        return resource.getParticipant().stream()
                .filter(p -> p.getActor() != null
                        && p.getActor().getReference() != null
                        && p.getActor().getReference().startsWith(typePrefix + "/"))
                .map(p -> p.getActor().getReferenceElement().getIdPart())
                .findFirst()
                .orElse(null);
    }

    default String extractParticipantName(Appointment resource, String typePrefix) {
        return resource.getParticipant().stream()
                .filter(p -> p.getActor() != null
                        && p.getActor().getReference() != null
                        && p.getActor().getReference().startsWith(typePrefix + "/")
                        && p.getActor().hasDisplay())
                .map(p -> p.getActor().getDisplay())
                .findFirst()
                .orElse(null);
    }

    default List<Appointment.AppointmentParticipantComponent> buildParticipants(String patientId) {
        Appointment.AppointmentParticipantComponent patient =
                new Appointment.AppointmentParticipantComponent()
                        .setActor(new Reference("Patient/" + patientId))
                        .setStatus(Appointment.ParticipationStatus.ACCEPTED);

        return List.of(patient);
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


