package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Appointment;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.CodeableReference;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Practitioner;
import org.hl7.fhir.r5.model.Reference;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.AppointmentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.CreateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.UpdateAppointmentRequestDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component("practitionerAppointmentMapper")
public class AppointmentMapper {

    private final MapperSupport support;

    public AppointmentMapper(MapperSupport support) {
        this.support = support;
    }

    public AppointmentDTO toDTO(Appointment resource) {
        if (resource == null) {
            return null;
        }

        return new AppointmentDTO(
                resource.getIdElement().getIdPart(),
                resource.hasStatus() ? resource.getStatus().toCode() : null,
                toLocalDateTime(resource.hasStart() ? resource.getStart() : null),
                toLocalDateTime(resource.hasEnd() ? resource.getEnd() : null),
                extractReason(resource),
                extractPractitionerName(resource),
                extractPatientId(resource),
                extractLocationId(resource)
        );
    }

    public Appointment toResource(CreateAppointmentRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Appointment resource = new Appointment();

        if (dto.start() != null) {
            resource.setStart(toDate(dto.start()));
        }

        if (dto.end() != null) {
            resource.setEnd(toDate(dto.end()));
        }

        if (dto.reason() != null && !dto.reason().isBlank()) {
            resource.setReason(List.of(
                    new CodeableReference(new CodeableConcept().setText(dto.reason()))
            ));
        }

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.addParticipant().setActor(support.toPatientReference(dto.patientId()));
        }

        if (dto.locationId() != null && !dto.locationId().isBlank()) {
            resource.addParticipant().setActor(support.toLocationReference(dto.locationId()));
        }

        resource.setStatus(Appointment.AppointmentStatus.BOOKED);
        return resource;
    }

    public void updateResource(UpdateAppointmentRequestDTO dto, Appointment resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.start() != null) {
            resource.setStart(toDate(dto.start()));
        }

        if (dto.end() != null) {
            resource.setEnd(toDate(dto.end()));
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Appointment.AppointmentStatus.fromCode(dto.status()));
        }
    }

    private String extractReason(Appointment resource) {
        if (!resource.hasReason() || resource.getReason().isEmpty()) {
            return null;
        }

        CodeableReference reason = resource.getReasonFirstRep();
        if (reason == null || !reason.hasConcept()) {
            return null;
        }

        return support.codeableConceptToText(reason.getConcept());
    }

    private String extractPatientId(Appointment resource) {
        for (Appointment.AppointmentParticipantComponent participant : resource.getParticipant()) {
            if (participant == null || !participant.hasActor()) {
                continue;
            }

            Reference actor = participant.getActor();
            if (actor.hasReference() && actor.getReference().startsWith("Patient/")) {
                return support.referenceToId(actor);
            }
        }
        return null;
    }

    private String extractLocationId(Appointment resource) {
        for (Appointment.AppointmentParticipantComponent participant : resource.getParticipant()) {
            if (participant == null || !participant.hasActor()) {
                continue;
            }

            Reference actor = participant.getActor();
            if (actor.hasReference() && actor.getReference().startsWith("Location/")) {
                return support.referenceToId(actor);
            }
        }
        return null;
    }

    private String extractPractitionerName(Appointment resource) {
        for (Appointment.AppointmentParticipantComponent participant : resource.getParticipant()) {
            if (participant == null || !participant.hasActor()) {
                continue;
            }

            Reference actor = participant.getActor();
            if (!actor.hasReference() || !actor.getReference().startsWith("Practitioner/")) {
                continue;
            }

            if (actor.getResource() instanceof Practitioner practitioner && practitioner.hasName()) {
                HumanName name = practitioner.getNameFirstRep();
                String given = name.hasGiven() && !name.getGiven().isEmpty()
                        ? name.getGiven().get(0).getValue()
                        : null;
                String family = name.hasFamily() ? name.getFamily() : null;

                if (given != null && family != null) {
                    return given + " " + family;
                }
                if (given != null) {
                    return given;
                }
                if (family != null) {
                    return family;
                }
            }

            return support.referenceToId(actor);
        }

        return null;
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