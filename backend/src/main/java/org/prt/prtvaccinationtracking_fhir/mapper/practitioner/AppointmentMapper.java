package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.*;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface AppointmentMapper {

    /// FHIR to DTO
    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "status",
            expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    @Mapping(target = "description",
            source = "description")
    @Mapping(target = "start",
            expression = "java(toLocalDateTime(resource.getStartElement()))")
    @Mapping(target = "end",
            expression = "java(toLocalDateTime(resource.getEndElement()))")
    @Mapping(target = "patientId",
            expression = "java(extractParticipant(resource, \"Patient\"))")
    @Mapping(target = "practitionerId",
            expression = "java(extractParticipant(resource, \"Practitioner\"))")
    @Mapping(target = "reason",
            expression = "java(resource.hasReason() ? resource.getReasonFirstRep().getText() : null)")

    AppointmentDTO toDTO(Appointment resource);

    /// DTO to FHIR
    ///
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status",
            expression = "java(dto.status() != null ? org.hl7.fhir.r5.model.Enumerations.AppointmentStatus.fromCode(dto.status()) : null)")
    @Mapping(target = "description",
            source = "description")
    @Mapping(target = "start",
            expression = "java(toDate(dto.start()))")
    @Mapping(target = "end",
            expression = "java(toDate(dto.end()))")
    @Mapping(target = "participant",
            expression = "java(buildParticipants(dto.patientId(), dto.practitionerId()))")
    @Mapping(target = "reason",
            expression = "java(dto.reason() != null ? java.util.List.of(toCodeableConcept(dto.reason())) : null)")

    Appointment toResource(CreateAppointmentRequestDTO dto);

    /// DTO to FHIR resource - update
    @Mapping(target = "status",
            expression = "java(dto.status() != null ? org.hl7.fhir.r5.model.Enumerations.AppointmentStatus.fromCode(dto.status()) : resource.getStatus())")
    @Mapping(target = "description",
            expression = "java(dto.description() != null ? dto.description() : resource.getDescription())")
    @Mapping(target = "start",
            expression = "java(dto.start() != null ? toDate(dto.start()) : resource.getStart())")
    @Mapping(target = "end",
            expression = "java(dto.end() != null ? toDate(dto.end()) : resource.getEnd())")

    void updateResourceFromDTO(UpdateAppointmentRequestDTO dto,
                               @MappingTarget Appointment resource);

    /// HELPERS

    default String extractParticipant(Appointment resource, String type) {
        return resource.getParticipant().stream()
                .filter(p -> p.getActor() != null
                        && p.getActor().getReference() != null
                        && p.getActor().getReference().startsWith(type))
                .map(p -> p.getActor().getReferenceElement().getIdPart())
                .findFirst()
                .orElse(null);
    }

    default List<Appointment.AppointmentParticipantComponent> buildParticipants(
            String patientId,
            String practitionerId
    ) {

        Appointment.AppointmentParticipantComponent patient =
                new Appointment.AppointmentParticipantComponent()
                        .setActor(new Reference("Patient/" + patientId))
                        .setStatus(Appointment.ParticipationStatus.ACCEPTED);

        Appointment.AppointmentParticipantComponent practitioner =
                new Appointment.AppointmentParticipantComponent()
                        .setActor(new Reference("Practitioner/" + practitionerId))
                        .setStatus(Appointment.ParticipationStatus.ACCEPTED);

        return List.of(patient, practitioner);
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

    default CodeableConcept toCodeableConcept(String text) {
        CodeableConcept concept = new CodeableConcept();
        concept.setText(text);
        return concept;
    }
}