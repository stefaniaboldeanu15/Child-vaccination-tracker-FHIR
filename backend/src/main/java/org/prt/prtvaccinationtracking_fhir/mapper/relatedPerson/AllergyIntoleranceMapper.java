package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.AllergyIntolerance;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.Enumeration;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.AllergyIntoleranceDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.CreateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.UpdateAllergyIntoleranceRequestDTO;
import org.springframework.stereotype.Component;

import java.util.List;

@Component("relatedpersonAllergyIntoleranceMapper")
public class AllergyIntoleranceMapper {

    private static final String CLINICAL_STATUS_SYSTEM =
            "http://terminology.hl7.org/CodeSystem/allergyintolerance-clinical";

    private static final String TYPE_SYSTEM =
            "http://terminology.hl7.org/CodeSystem/allergy-intolerance-type";

    private final MapperSupport support;

    public AllergyIntoleranceMapper(MapperSupport support) {
        this.support = support;
    }

    public AllergyIntoleranceDTO toDTO(AllergyIntolerance resource) {
        if (resource == null) {
            return null;
        }

        return new AllergyIntoleranceDTO(
                resource.getIdElement().getIdPart(),
                mapClinicalStatus(resource.getClinicalStatus()),
                mapType(resource.getType()),
                mapCategory(resource.getCategory()),
                mapCriticality(resource.getCriticality()),
                resource.hasCode() ? support.codeableConceptToText(resource.getCode()) : null,
                resource.hasPatient() ? support.referenceToId(resource.getPatient()) : null,
                resource.hasEncounter() ? support.referenceToId(resource.getEncounter()) : null
        );
    }

    public AllergyIntolerance toResource(CreateAllergyIntoleranceRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        AllergyIntolerance resource = new AllergyIntolerance();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setPatient(support.toPatientReference(dto.patientId()));
        }

        if (dto.encounterId() != null && !dto.encounterId().isBlank()) {
            resource.setEncounter(support.toEncounterReference(dto.encounterId()));
        }

        if (dto.code() != null && !dto.code().isBlank()) {
            resource.setCode(new CodeableConcept().setText(dto.code()));
        }

        if (dto.clinicalStatus() != null) {
            resource.setClinicalStatus(toClinicalStatusConcept(dto.clinicalStatus()));
        }

        if (dto.type() != null) {
            resource.setType(toTypeConcept(dto.type()));
        }

        if (dto.category() != null) {
            resource.setCategory(List.of(
                    new Enumeration<>(
                            new AllergyIntolerance.AllergyIntoleranceCategoryEnumFactory(),
                            toFhirCategory(dto.category())
                    )
            ));
        }

        if (dto.criticality() != null) {
            resource.setCriticality(toFhirCriticality(dto.criticality()));
        }

        return resource;
    }

    public void updateResource(UpdateAllergyIntoleranceRequestDTO dto, AllergyIntolerance resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.clinicalStatus() != null) {
            resource.setClinicalStatus(toClinicalStatusConcept(dto.clinicalStatus()));
        }

        if (dto.criticality() != null) {
            resource.setCriticality(toFhirCriticality(dto.criticality()));
        }
    }

    private AllergyIntoleranceDTO.AllergyClinicalStatus mapClinicalStatus(CodeableConcept concept) {
        if (concept == null) {
            return null;
        }

        String value = firstCodeOrText(concept);
        if (value == null) {
            return null;
        }

        return switch (normalize(value)) {
            case "active" -> AllergyIntoleranceDTO.AllergyClinicalStatus.ACTIVE;
            case "inactive" -> AllergyIntoleranceDTO.AllergyClinicalStatus.INACTIVE;
            case "resolved" -> AllergyIntoleranceDTO.AllergyClinicalStatus.RESOLVED;
            default -> null;
        };
    }

    private CodeableConcept toClinicalStatusConcept(AllergyIntoleranceDTO.AllergyClinicalStatus status) {
        if (status == null) {
            return null;
        }

        String code = switch (status) {
            case ACTIVE -> "active";
            case INACTIVE -> "inactive";
            case RESOLVED -> "resolved";
        };

        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(CLINICAL_STATUS_SYSTEM)
                        .setCode(code)
                        .setDisplay(code))
                .setText(code);
    }

    private AllergyIntoleranceDTO.AllergyType mapType(CodeableConcept concept) {
        if (concept == null) {
            return null;
        }

        String value = firstCodeOrText(concept);
        if (value == null) {
            return null;
        }

        return switch (normalize(value)) {
            case "allergy" -> AllergyIntoleranceDTO.AllergyType.ALLERGY;
            case "intolerance" -> AllergyIntoleranceDTO.AllergyType.INTOLERANCE;
            default -> null;
        };
    }

    private CodeableConcept toTypeConcept(AllergyIntoleranceDTO.AllergyType type) {
        if (type == null) {
            return null;
        }

        String code = switch (type) {
            case ALLERGY -> "allergy";
            case INTOLERANCE -> "intolerance";
        };

        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(TYPE_SYSTEM)
                        .setCode(code)
                        .setDisplay(code))
                .setText(code);
    }

    private AllergyIntoleranceDTO.AllergyIntoleranceCategory mapCategory(
            List<Enumeration<AllergyIntolerance.AllergyIntoleranceCategory>> categories
    ) {
        if (categories == null || categories.isEmpty()) {
            return null;
        }

        Enumeration<AllergyIntolerance.AllergyIntoleranceCategory> first = categories.get(0);
        if (first == null || first.getValue() == null) {
            return null;
        }

        return switch (first.getValue()) {
            case FOOD -> AllergyIntoleranceDTO.AllergyIntoleranceCategory.FOOD;
            case MEDICATION -> AllergyIntoleranceDTO.AllergyIntoleranceCategory.MEDICATION;
            case ENVIRONMENT -> AllergyIntoleranceDTO.AllergyIntoleranceCategory.ENVIRONMENT;
            case BIOLOGIC -> AllergyIntoleranceDTO.AllergyIntoleranceCategory.BIOLOGIC;
            default -> null;
        };
    }

    private AllergyIntolerance.AllergyIntoleranceCategory toFhirCategory(
            AllergyIntoleranceDTO.AllergyIntoleranceCategory category
    ) {
        if (category == null) {
            return null;
        }

        return switch (category) {
            case FOOD -> AllergyIntolerance.AllergyIntoleranceCategory.FOOD;
            case MEDICATION -> AllergyIntolerance.AllergyIntoleranceCategory.MEDICATION;
            case ENVIRONMENT -> AllergyIntolerance.AllergyIntoleranceCategory.ENVIRONMENT;
            case BIOLOGIC -> AllergyIntolerance.AllergyIntoleranceCategory.BIOLOGIC;
        };
    }

    private AllergyIntoleranceDTO.AllergyCriticality mapCriticality(
            AllergyIntolerance.AllergyIntoleranceCriticality criticality
    ) {
        if (criticality == null) {
            return null;
        }

        return switch (criticality) {
            case LOW -> AllergyIntoleranceDTO.AllergyCriticality.LOW;
            case HIGH -> AllergyIntoleranceDTO.AllergyCriticality.HIGH;
            case UNABLETOASSESS -> AllergyIntoleranceDTO.AllergyCriticality.UNABLE_TO_ASSESS;
            default -> null;
        };
    }

    private AllergyIntolerance.AllergyIntoleranceCriticality toFhirCriticality(
            AllergyIntoleranceDTO.AllergyCriticality criticality
    ) {
        if (criticality == null) {
            return null;
        }

        return switch (criticality) {
            case LOW -> AllergyIntolerance.AllergyIntoleranceCriticality.LOW;
            case HIGH -> AllergyIntolerance.AllergyIntoleranceCriticality.HIGH;
            case UNABLE_TO_ASSESS -> AllergyIntolerance.AllergyIntoleranceCriticality.UNABLETOASSESS;
        };
    }

    private String firstCodeOrText(CodeableConcept concept) {
        if (concept == null) {
            return null;
        }
        if (concept.hasCoding() && concept.getCodingFirstRep().hasCode()) {
            return concept.getCodingFirstRep().getCode();
        }
        if (concept.hasText()) {
            return concept.getText();
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? null : value.trim().toLowerCase();
    }
}