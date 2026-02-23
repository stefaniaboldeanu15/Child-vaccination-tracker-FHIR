package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.*;

@Mapper
        (config = BaseMapperConfig.class)
public interface AllergyIntoleranceMapper {

     /// FHIR to DTO
    @Mapping(target = "id",
            expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "clinicalStatus",
            expression = "java(mapClinicalStatus(resource.getClinicalStatus()))")
    @Mapping(target = "type",
            expression = "java(mapType(resource.getType()))")
    @Mapping(target = "category",
            expression = "java(resource.hasCategory() ? mapCategory(resource.getCategoryFirstRep()) : null)")
    @Mapping(target = "criticality",
            expression = "java(mapCriticality(resource.getCriticality()))")
    @Mapping(target = "code",
            expression = "java(resource.getCode() != null ? resource.getCode().getText() : null)")
    @Mapping(target = "patientId",
            expression = "java(resource.getPatient().getReferenceElement().getIdPart())")
    @Mapping(target = "encounterId",
            expression = "java(resource.getEncounter() != null ? resource.getEncounter().getReferenceElement().getIdPart() : null)")

    AllergyIntoleranceDTO toDTO(AllergyIntolerance resource);

    /// CREATE DTO → FHIR

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "clinicalStatus",
            expression = "java(mapClinicalStatus(dto.clinicalStatus()))")
    @Mapping(target = "type",
            expression = "java(mapType(dto.type()))")
    @Mapping(target = "category",
            expression = "java(dto.category() != null ? java.util.Collections.singletonList(mapCategory(dto.category())) : null)")
    @Mapping(target = "criticality",
            expression = "java(mapCriticality(dto.criticality()))")
    @Mapping(target = "code",
            expression = "java(toCodeableConcept(dto.code()))")
    @Mapping(target = "patient",
            expression = "java(new Reference(\"Patient/\" + dto.patientId()))")
    @Mapping(target = "encounter",
            expression = "java(dto.encounterId() != null ? new Reference(\"Encounter/\" + dto.encounterId()) : null)")
    AllergyIntolerance toResource(CreateAllergyIntoleranceRequestDTO dto);

    /// update DTO to FHIR resources
    @Mapping(target = "clinicalStatus",
            expression = "java(dto.clinicalStatus() != null ? mapClinicalStatus(dto.clinicalStatus()) : resource.getClinicalStatus())")
    @Mapping(target = "criticality",
            expression = "java(dto.criticality() != null ? mapCriticality(dto.criticality()) : resource.getCriticality())")
    void updateResourceFromDTO(UpdateAllergyIntoleranceRequestDTO dto,
                               @MappingTarget AllergyIntolerance resource);

    /// helpers

    default AllergyIntoleranceDTO.AllergyClinicalStatus mapClinicalStatus(CodeableConcept status) {
        if (status == null || status.getCodingFirstRep() == null) return null;

        return AllergyIntoleranceDTO.AllergyClinicalStatus
                .valueOf(status.getCodingFirstRep().getCode().toUpperCase());
    }

    default CodeableConcept mapClinicalStatus(AllergyIntoleranceDTO.AllergyClinicalStatus status) {
        if (status == null) return null;

        CodeableConcept concept = new CodeableConcept();
        concept.addCoding()
                .setSystem("http://hl7.org/fhir/allergyintolerance-clinical")
                .setCode(status.name().toLowerCase());

        return concept;
    }


    default AllergyIntoleranceDTO.AllergyType mapType(CodeableConcept type) {
        if (type == null || type.getCodingFirstRep() == null) return null;

        return AllergyIntoleranceDTO.AllergyType
                .valueOf(type.getCodingFirstRep().getCode().toUpperCase());
    }

    default CodeableConcept mapType(AllergyIntoleranceDTO.AllergyType type) {
        if (type == null) return null;

        CodeableConcept concept = new CodeableConcept();
        concept.addCoding()
                .setSystem("http://hl7.org/fhir/allergy-intolerance-type")
                .setCode(type.name().toLowerCase());

        return concept;
    }

    default AllergyIntoleranceDTO.AllergyIntoleranceCategory mapCategory(CodeableConcept category) {
        if (category == null || category.getCodingFirstRep() == null) return null;

        return AllergyIntoleranceDTO.AllergyIntoleranceCategory
                .valueOf(category.getCodingFirstRep().getCode().toUpperCase());
    }

    default CodeableConcept mapCategory(AllergyIntoleranceDTO.AllergyIntoleranceCategory category) {
        if (category == null) return null;

        CodeableConcept concept = new CodeableConcept();
        concept.addCoding()
                .setSystem("http://hl7.org/fhir/allergy-intolerance-category")
                .setCode(category.name().toLowerCase());

        return concept;
    }

    default AllergyIntoleranceDTO.AllergyCriticality mapCriticality(AllergyIntolerance.AllergyIntoleranceCriticality criticality) {
        return criticality != null
                ? AllergyIntoleranceDTO.AllergyCriticality.valueOf(criticality.name())
                : null;
    }

    default AllergyIntolerance.AllergyIntoleranceCriticality mapCriticality(AllergyIntoleranceDTO.AllergyCriticality criticality) {
        return criticality != null
                ? AllergyIntolerance.AllergyIntoleranceCriticality.valueOf(criticality.name())
                : null;
    }

    default CodeableConcept toCodeableConcept(String text) {
        if (text == null) return null;

        CodeableConcept concept = new CodeableConcept();
        concept.setText(text);
        return concept;
    }
}