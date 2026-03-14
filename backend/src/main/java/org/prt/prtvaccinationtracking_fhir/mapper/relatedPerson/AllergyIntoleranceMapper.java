package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.AllergyIntoleranceDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper(config = BaseMapperConfig.class)
public interface AllergyIntoleranceMapper {

    @Mapping(target = "allergyId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getPatient() != null ? resource.getPatient().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "clinicalStatus", expression = "java(resource.hasClinicalStatus() && resource.getClinicalStatus().hasCoding() ? resource.getClinicalStatus().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "verificationStatus", expression = "java(resource.hasVerificationStatus() && resource.getVerificationStatus().hasCoding() ? resource.getVerificationStatus().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "code", expression = "java(resource.hasCode() && resource.getCode().hasCoding() ? resource.getCode().getCodingFirstRep().getCode() : null)")
    @Mapping(target = "display", expression = "java(resource.hasCode() ? resource.getCode().getText() : null)")
    @Mapping(target = "criticality", expression = "java(resource.hasCriticality() ? resource.getCriticality().toCode() : null)")
    @Mapping(target = "severity", expression = "java(extractSeverity(resource))")
    @Mapping(target = "reaction", expression = "java(extractReactionText(resource))")
    @Mapping(target = "onsetDate", expression = "java(resource.hasOnset() && resource.getOnset() instanceof DateTimeType dt ? toDateString(dt.getValue()) : null)")
    @Mapping(target = "recorder", expression = "java(resource.getRecorder() != null ? resource.getRecorder().getDisplay() : null)")
    AllergyIntoleranceDTO toDTO(AllergyIntolerance resource);

    default String extractSeverity(AllergyIntolerance resource) {
        if (resource == null || !resource.hasReaction() || resource.getReaction().isEmpty()) return null;
        AllergyIntolerance.AllergyIntoleranceReactionComponent r = resource.getReactionFirstRep();
        return r.hasSeverity() ? r.getSeverity().toCode() : null;
    }

                            default String extractReactionText(AllergyIntolerance resource) {
        if (resource == null || !resource.hasReaction() || resource.getReaction().isEmpty()) return null;
        AllergyIntolerance.AllergyIntoleranceReactionComponent r = resource.getReactionFirstRep();
        if (r.hasDescription()) return r.getDescription();
        if (r.hasManifestation() && !r.getManifestation().isEmpty()) {
            CodeableReference ref = r.getManifestationFirstRep();
            if (ref.hasConcept() && ref.getConcept().hasText()) {
                return ref.getConcept().getText();
            }
        }
        return null;
    }

    default String toDateString(Date date) {
        if (date == null) return null;
        LocalDate local = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local.toString();
    }
}


