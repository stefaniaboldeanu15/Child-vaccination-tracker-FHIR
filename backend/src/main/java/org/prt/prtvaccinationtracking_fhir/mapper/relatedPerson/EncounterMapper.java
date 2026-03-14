package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.CreateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.EncounterDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.UpdateEncounterRequestDTO;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component("relatedpersonEncounterMapper")
public class EncounterMapper {

    private final MapperSupport support;

    public EncounterMapper(MapperSupport support) {
        this.support = support;
    }

    public EncounterDTO toDTO(Encounter resource) {
        if (resource == null) {
            return null;
        }

        return new EncounterDTO(
                resource.getIdElement().getIdPart(),
                resource.hasSubject() ? support.referenceToId(resource.getSubject()) : null,
                extractStart(resource),
                extractEnd(resource),
                extractReason(resource),
                extractLocation(resource),
                resource.hasStatus() ? resource.getStatus().toCode() : null,
                extractPractitionerName(resource)
        );
    }

    public Encounter toResource(CreateEncounterRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Encounter resource = new Encounter();

        if (dto.start() != null || dto.end() != null) {
            resource.setActualPeriod(toPeriod(dto.start(), dto.end()));
        }

        if (dto.reason() != null && !dto.reason().isBlank()) {
            resource.setReason(List.of(
                    new Encounter.ReasonComponent()
                            .setUse(List.of(new CodeableConcept().setText(dto.reason())))
            ));
        }

        if (dto.location() != null && !dto.location().isBlank()) {
            resource.setLocation(List.of(
                    new Encounter.EncounterLocationComponent()
                            .setLocation(new Reference(dto.location()))
            ));
        }

        resource.setStatus(Enumerations.EncounterStatus.INPROGRESS);
        return resource;
    }

    public void updateResource(UpdateEncounterRequestDTO dto, Encounter resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.start() != null || dto.end() != null) {
            Period period = resource.hasActualPeriod() ? resource.getActualPeriod() : new Period();

            if (dto.start() != null) {
                period.setStart(toDate(dto.start()));
            }

            if (dto.end() != null) {
                period.setEnd(toDate(dto.end()));
            }

            resource.setActualPeriod(period);
        }

        if (dto.reason() != null) {
            if (dto.reason().isBlank()) {
                resource.setReason(new ArrayList<>());
            } else {
                resource.setReason(List.of(
                        new Encounter.ReasonComponent()
                                .setUse(List.of(new CodeableConcept().setText(dto.reason())))
                ));
            }
        }

        if (dto.location() != null) {
            if (dto.location().isBlank()) {
                resource.setLocation(new ArrayList<>());
            } else {
                resource.setLocation(List.of(
                        new Encounter.EncounterLocationComponent()
                                .setLocation(new Reference(dto.location()))
                ));
            }
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Enumerations.EncounterStatus.fromCode(dto.status()));
        }
    }

    private String extractReason(Encounter resource) {
        if (!resource.hasReason() || resource.getReason().isEmpty()) {
            return null;
        }

        Encounter.ReasonComponent reason = resource.getReasonFirstRep();

        if (reason.hasUse() && !reason.getUse().isEmpty()) {
            return support.codeableConceptToText(reason.getUseFirstRep());
        }

        if (reason.hasValue() && !reason.getValue().isEmpty()) {
            CodeableReference value = reason.getValueFirstRep();

            if (value.hasConcept()) {
                return support.codeableConceptToText(value.getConcept());
            }

            if (value.hasReference()) {
                Reference reference = value.getReference();
                if (reference.hasDisplay()) {
                    return reference.getDisplay();
                }
                return support.referenceToId(reference);
            }
        }

        return null;
    }

    private String extractLocation(Encounter resource) {
        if (!resource.hasLocation() || resource.getLocation().isEmpty()) {
            return null;
        }

        Encounter.EncounterLocationComponent location = resource.getLocationFirstRep();
        if (!location.hasLocation()) {
            return null;
        }

        Reference reference = location.getLocation();
        if (reference.hasDisplay()) {
            return reference.getDisplay();
        }

        return reference.hasReference() ? reference.getReference() : null;
    }

    private String extractPractitionerName(Encounter resource) {
        if (!resource.hasParticipant() || resource.getParticipant().isEmpty()) {
            return null;
        }

        for (Encounter.EncounterParticipantComponent participant : resource.getParticipant()) {
            if (participant == null || !participant.hasActor()) {
                continue;
            }

            Reference actor = participant.getActor();

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

            if (actor.hasDisplay()) {
                return actor.getDisplay();
            }
        }

        return null;
    }

    private LocalDateTime extractStart(Encounter resource) {
        if (resource.hasActualPeriod() && resource.getActualPeriod().hasStart()) {
            return toLocalDateTime(resource.getActualPeriod().getStart());
        }
        return null;
    }

    private LocalDateTime extractEnd(Encounter resource) {
        if (resource.hasActualPeriod() && resource.getActualPeriod().hasEnd()) {
            return toLocalDateTime(resource.getActualPeriod().getEnd());
        }
        return null;
    }

    private Period toPeriod(LocalDateTime start, LocalDateTime end) {
        Period period = new Period();

        if (start != null) {
            period.setStart(toDate(start));
        }

        if (end != null) {
            period.setEnd(toDate(end));
        }

        return period;
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