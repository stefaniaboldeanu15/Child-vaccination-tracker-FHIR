package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.Reference;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.CreateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.EncounterDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.UpdateEncounterRequestDTO;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface EncounterMapper {

    // =========================
    // FHIR → DTO
    // =========================
    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getSubject() != null ? resource.getSubject().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "start", expression = "java(resource.hasPeriod() ? toLocalDateTime(resource.getPeriod().getStartElement()) : null)")
    @Mapping(target = "end", expression = "java(resource.hasPeriod() ? toLocalDateTime(resource.getPeriod().getEndElement()) : null)")
    @Mapping(target = "reason", expression = "java(resource.hasReasonCode() ? resource.getReasonCodeFirstRep().getText() : null)")
    @Mapping(target = "location", expression = "java(extractLocation(resource))")
    @Mapping(target = "status", expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    @Mapping(target = "practitionerName", expression = "java(extractPractitionerDisplay(resource))")
    EncounterDTO toDTO(Encounter resource);

    // =========================
    // CREATE DTO → FHIR
    // patientId & practitionerId come from service/security
    // =========================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(org.hl7.fhir.r5.model.Encounter.EncounterStatus.INPROGRESS)")
    @Mapping(target = "subject", expression = "java(toPatientRef(patientId))")
    @Mapping(target = "period", expression = "java(buildPeriod(dto.start(), dto.end()))")
    @Mapping(target = "reasonCode", expression = "java(dto.reason() != null ? List.of(toTextConcept(dto.reason())) : null)")
    @Mapping(target = "location", expression = "java(dto.location() != null ? List.of(buildEncounterLocation(dto.location())) : null)")
    @Mapping(target = "participant", expression = "java(toParticipants(practitionerId))")
    Encounter toResource(CreateEncounterRequestDTO dto,
                         @Context String patientId,
                         @Context String practitionerId);

    // =========================
    // UPDATE DTO → EXISTING FHIR
    // =========================
    @Mapping(target = "status",
            expression = "java(dto.status() != null ? org.hl7.fhir.r5.model.Encounter.EncounterStatus.fromCode(dto.status()) : resource.getStatus())")
    @Mapping(target = "period",
            expression = "java(dto.start() != null || dto.end() != null ? mergePeriod(resource.getPeriod(), dto.start(), dto.end()) : resource.getPeriod())")
    @Mapping(target = "reasonCode",
            expression = "java(dto.reason() != null ? List.of(toTextConcept(dto.reason())) : resource.getReasonCode())")
    @Mapping(target = "location",
            expression = "java(dto.location() != null ? List.of(buildEncounterLocation(dto.location())) : resource.getLocation())")
    void updateResourceFromDTO(UpdateEncounterRequestDTO dto,
                               @MappingTarget Encounter resource);

    // =========================
    // Helpers
    // =========================
    default Reference toPatientRef(String patientId) {
        return patientId == null ? null : new Reference("Patient/" + patientId);
    }

    default List<Encounter.EncounterParticipantComponent> toParticipants(String practitionerId) {
        if (practitionerId == null) return null;
        Encounter.EncounterParticipantComponent p = new Encounter.EncounterParticipantComponent();
        p.setActor(new Reference("Practitioner/" + practitionerId));
        return List.of(p);
    }

    default CodeableConcept toTextConcept(String text) {
        CodeableConcept cc = new CodeableConcept();
        cc.setText(text);
        return cc;
    }

    default String extractLocation(Encounter resource) {
        if (!resource.hasLocation() || resource.getLocationFirstRep().getLocation() == null) return null;
        Reference ref = resource.getLocationFirstRep().getLocation();
        if (ref.getReferenceElement() != null && ref.getReferenceElement().hasIdPart()) {
            return ref.getReferenceElement().getIdPart();
        }
        return ref.getDisplay();
    }

    default String extractPractitionerDisplay(Encounter resource) {
        if (!resource.hasParticipant()) return null;
        return resource.getParticipant().stream()
                .filter(p -> p.getActor() != null && p.getActor().hasDisplay())
                .map(p -> p.getActor().getDisplay())
                .findFirst()
                .orElse(null);
    }

    default Period buildPeriod(LocalDateTime start, LocalDateTime end) {
        if (start == null && end == null) return null;
        Period p = new Period();
        if (start != null) p.setStart(toDate(start));
        if (end != null) p.setEnd(toDate(end));
        return p;
    }

    default Period mergePeriod(Period existing, LocalDateTime start, LocalDateTime end) {
        Period p = existing != null ? existing : new Period();
        if (start != null) p.setStart(toDate(start));
        if (end != null) p.setEnd(toDate(end));
        return p;
    }

    default Encounter.EncounterLocationComponent buildEncounterLocation(String locationValue) {
        Encounter.EncounterLocationComponent c = new Encounter.EncounterLocationComponent();
        // If you store an ID, use: new Reference("Location/" + locationValue)
        Reference ref = new Reference();
        ref.setDisplay(locationValue);
        c.setLocation(ref);
        return c;
    }

    default Date toDate(LocalDateTime ldt) {
        return Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant());
    }

    default LocalDateTime toLocalDateTime(DateTimeType dateTimeType) {
        if (dateTimeType == null || dateTimeType.getValue() == null) return null;
        return LocalDateTime.ofInstant(dateTimeType.getValue().toInstant(), ZoneId.systemDefault());
    }
}