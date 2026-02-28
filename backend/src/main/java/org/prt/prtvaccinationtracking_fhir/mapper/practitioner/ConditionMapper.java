package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.condition.*;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;


@Mapper(config = BaseMapperConfig.class)
public interface ConditionMapper {

     /// FHIR → DTO

    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId",
            expression = "java(resource.getSubject().getReferenceElement().getIdPart())")
    @Mapping(target = "code",
            expression = "java(resource.getCode() != null && resource.getCode().hasCoding() ? resource.getCode().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "display",
            expression = "java(resource.getCode() != null && resource.getCode().hasCoding() ? resource.getCode().getCodingFirstRep().getDisplay() : null)")
    @Mapping(target = "clinicalStatus",
            expression = "java(resource.getClinicalStatus() != null ? resource.getClinicalStatus().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "verificationStatus",
            expression = "java(resource.getVerificationStatus() != null ? resource.getVerificationStatus().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "onsetDate",
            expression = "java(resource.getOnsetDateTimeType() != null ? toLocalDate(resource.getOnsetDateTimeType()) : null)")
    @Mapping(target = "recorderName",
            expression = "java(resource.getRecorder() != null ? resource.getRecorder().getDisplay() : null)")
    @Mapping(target = "notes",
            expression = "java(resource.hasNote() ? resource.getNoteFirstRep().getText() : null)")
    ConditionDTO toDTO(Condition resource);

     ///CREATE DTO → FHIR

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "code",
            expression = "java(buildCodeableConcept(dto.code(), dto.display()))")
    @Mapping(target = "clinicalStatus",
            expression = "java(dto.clinicalStatus() != null ? buildSimpleConcept(\"http://terminology.hl7.org/CodeSystem/condition-clinical\", dto.clinicalStatus()) : null)")
    @Mapping(target = "verificationStatus",
            expression = "java(dto.verificationStatus() != null ? buildSimpleConcept(\"http://terminology.hl7.org/CodeSystem/condition-ver-status\", dto.verificationStatus()) : null)")
    @Mapping(target = "onset",
            expression = "java(dto.onsetDate() != null ? new DateTimeType(toDate(dto.onsetDate())) : null)")
    @Mapping(target = "note",
            expression = "java(dto.notes() != null ? List.of(buildAnnotation(dto.notes())) : null)")

    Condition toResource(CreateConditionRequestDTO dto);

    /// UPDATE DTO → EXISTING RESOURCE

    @Mapping(target = "clinicalStatus",
            expression = "java(dto.clinicalStatus() != null ? buildSimpleConcept(\"http://terminology.hl7.org/CodeSystem/condition-clinical\", dto.clinicalStatus()) : resource.getClinicalStatus())")
    @Mapping(target = "verificationStatus",
            expression = "java(dto.verificationStatus() != null ? buildSimpleConcept(\"http://terminology.hl7.org/CodeSystem/condition-ver-status\", dto.verificationStatus()) : resource.getVerificationStatus())")
    @Mapping(target = "note",
            expression = "java(dto.notes() != null ? List.of(buildAnnotation(dto.notes())) : resource.getNote())")
    void updateResourceFromDTO(UpdateConditionRequestDTO dto,
                               @MappingTarget Condition resource);

     ///HELPERS

    default CodeableConcept buildCodeableConcept(String code, String display) {
        if (code == null) return null;

        CodeableConcept concept = new CodeableConcept();
        concept.addCoding()
                .setSystem("http://snomed.info/sct")
                .setCode(code)
                .setDisplay(display);

        return concept;
    }

    default CodeableConcept buildSimpleConcept(String system, String code) {
        CodeableConcept concept = new CodeableConcept();
        concept.addCoding()
                .setSystem(system)
                .setCode(code);
        return concept;
    }

    default Annotation buildAnnotation(String text) {
        Annotation annotation = new Annotation();
        annotation.setText(text);
        return annotation;
    }

    default LocalDate toLocalDate(DateTimeType dateTime) {
        if (dateTime == null || dateTime.getValue() == null) return null;

        return dateTime.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    default Date toDate(LocalDate localDate) {
        if (localDate == null) return null;

        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}