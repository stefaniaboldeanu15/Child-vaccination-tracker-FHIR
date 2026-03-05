package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Consent;
import org.hl7.fhir.r5.model.DateType;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent.ConsentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent.CreateConsentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent.UpdateConsentRequestDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

@Mapper(config = BaseMapperConfig.class)
public interface ConsentMapper {


    /// FHIR → DTO

    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    // Consent.subject - patientId
    @Mapping(target = "patientId",
            expression = "java(resource.getSubject() != null ? resource.getSubject().getReferenceElement().getIdPart() : null)")
    // Consent.grantor - relatedPersonId
    @Mapping(target = "relatedPersonId",
            expression = "java(extractGrantorRelatedPersonId(resource))")
    // Consent.category[0].text - scope
    @Mapping(target = "scope",
            expression = "java(resource.hasCategory() ? resource.getCategoryFirstRep().getText() : null)")
    // Consent.status - status
    @Mapping(target = "status",
            expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    // Consent.date - dateGiven
    @Mapping(target = "dateGiven",
            expression = "java(toLocalDate(resource.getDateElement()))")
    // Consent.verification.verifiedBy.display - recorderName
    @Mapping(target = "recorderName",
            expression = "java(extractRecorderName(resource))")
    ConsentDTO toDTO(Consent resource);

    /// CREATE DTO → FHIR

    @Mapping(target = "id", ignore = true)

    // Consent.status is required in R5
    @Mapping(target = "status",
            expression = "java(dto.status() != null ? Enumerations.ConsentState.fromCode(dto.status()) : Enumerations.ConsentState.DRAFT)")
    // Patient
    @Mapping(target = "subject",
            expression = "java(dto.patientId() != null ? new Reference(\"Patient/\" + dto.patientId()) : null)")
    // Consent.date
    @Mapping(target = "date",
            expression = "java(toDateType(dto.dateGiven()))")
    //  scope as category.text
    @Mapping(target = "category",
            expression = "java(dto.scope() != null ? List.of(toCodeableConcept(dto.scope())) : null)")
    // grantor = RelatedPerson/guardian
    @Mapping(target = "grantor",
            expression = "java(dto.relatedPersonId() != null ? List.of(new Reference(\"RelatedPerson/\" + dto.relatedPersonId())) : null)")

    Consent toResource(CreateConsentRequestDTO dto);

    /// UPDATE DTO → EXISTING FHIR

    @Mapping(target = "status",
            expression = "java(dto.status() != null ? Enumerations.ConsentState.fromCode(dto.status()) : resource.getStatus())")

    void updateResourceFromDTO(UpdateConsentRequestDTO dto,
                               @MappingTarget Consent resource);

    /// HELPERS

    default String extractGrantorRelatedPersonId(Consent resource) {
        if (!resource.hasGrantor()) return null;

        return resource.getGrantor().stream()
                .filter(ref -> ref.getReference() != null && ref.getReference().startsWith("RelatedPerson/"))
                .map(ref -> ref.getReferenceElement().getIdPart())
                .findFirst()
                .orElse(null);
    }

    default String extractRecorderName(Consent resource) {
        // Not a direct "recorder" field; using verification.verifiedBy.display when present
        if (resource.hasVerification()
                && resource.getVerificationFirstRep().hasVerifiedBy()) {
            return resource.getVerificationFirstRep().getVerifiedBy().getDisplay();
        }
        return null;
    }

    default LocalDate toLocalDate(DateType dateType) {
        if (dateType == null || dateType.getValue() == null) return null;

        return dateType.getValue().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    default DateType toDateType(LocalDate date) {
        if (date == null) return null;

        Date d = Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
        return new DateType(d);
    }

    default CodeableConcept toCodeableConcept(String text) {
        CodeableConcept cc = new CodeableConcept();
        cc.setText(text);
        return cc;
    }

    default Consent.ConsentPolicyBasisComponent buildPolicyBasis(String policyText) {
        Consent.ConsentPolicyBasisComponent basis = new Consent.ConsentPolicyBasisComponent();
        basis.setUrl(policyText);
        return basis;
    }
}