package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.hl7.fhir.r5.model.Reference;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.CreateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.ImmunizationRecommendationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.UpdateImmunizationRecommendationRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("practitionerRecommendationMapper")
public class RecommendationMapper {

    private final MapperSupport support;

    public RecommendationMapper(MapperSupport support) {
        this.support = support;
    }

    public ImmunizationRecommendationDTO toDTO(ImmunizationRecommendation resource) {
        if (resource == null) {
            return null;
        }

        return new ImmunizationRecommendationDTO(
                resource.getIdElement().getIdPart(),
                resource.hasPatient() ? support.referenceToId(resource.getPatient()) : null,
                extractVaccineCode(resource),
                extractVaccineDisplay(resource),
                extractStatus(resource),
                extractDueDate(resource),
                extractRecommendationSource(resource)
        );
    }

    public ImmunizationRecommendation toResource(CreateImmunizationRecommendationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        ImmunizationRecommendation resource = new ImmunizationRecommendation();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setPatient(support.toPatientReference(dto.patientId()));
        }

        if (dto.recommendationSource() != null && !dto.recommendationSource().isBlank()) {
            resource.setAuthority(new Reference().setDisplay(dto.recommendationSource()));
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent recommendation =
                new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent();

        if (dto.vaccineCode() != null || dto.vaccineDisplay() != null) {
            recommendation.setVaccineCode(List.of(toVaccineCode(dto.vaccineCode(), dto.vaccineDisplay())));
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            recommendation.setForecastStatus(new CodeableConcept().setText(dto.status()));
        }

        if (dto.dueDate() != null) {
            recommendation.setDateCriterion(List.of(
                    new ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent()
                            .setValue(support.toDate(dto.dueDate()))
            ));
        }

        resource.setRecommendation(List.of(recommendation));
        return resource;
    }

    public void updateResource(UpdateImmunizationRecommendationRequestDTO dto, ImmunizationRecommendation resource) {
        if (dto == null || resource == null) {
            return;
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent recommendation =
                resource.hasRecommendation()
                        ? resource.getRecommendationFirstRep()
                        : new ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent();

        if (dto.status() != null) {
            if (dto.status().isBlank()) {
                recommendation.setForecastStatus(null);
            } else {
                recommendation.setForecastStatus(new CodeableConcept().setText(dto.status()));
            }
        }

        if (dto.dueDate() != null) {
            recommendation.setDateCriterion(List.of(
                    new ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent()
                            .setValue(support.toDate(dto.dueDate()))
            ));
        }

        if (resource.hasRecommendation()) {
            resource.getRecommendation().set(0, recommendation);
        } else {
            resource.setRecommendation(List.of(recommendation));
        }
    }

    private CodeableConcept toVaccineCode(String code, String display) {
        CodeableConcept concept = new CodeableConcept();

        if (display != null && !display.isBlank()) {
            concept.setText(display);
        }

        if (code != null && !code.isBlank()) {
            concept.addCoding(new Coding().setCode(code).setDisplay(display));
        }

        return concept;
    }

    private String extractVaccineCode(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation()) {
            return null;
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent recommendation =
                resource.getRecommendationFirstRep();

        if (!recommendation.hasVaccineCode() || recommendation.getVaccineCode().isEmpty()) {
            return null;
        }

        CodeableConcept concept = recommendation.getVaccineCodeFirstRep();
        if (!concept.hasCoding()) {
            return null;
        }

        Coding coding = concept.getCodingFirstRep();
        return coding.hasCode() ? coding.getCode() : null;
    }

    private String extractVaccineDisplay(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation()) {
            return null;
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent recommendation =
                resource.getRecommendationFirstRep();

        if (!recommendation.hasVaccineCode() || recommendation.getVaccineCode().isEmpty()) {
            return null;
        }

        return support.codeableConceptToText(recommendation.getVaccineCodeFirstRep());
    }

    private String extractStatus(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation()) {
            return null;
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent recommendation =
                resource.getRecommendationFirstRep();

        if (!recommendation.hasForecastStatus()) {
            return null;
        }

        return support.codeableConceptToText(recommendation.getForecastStatus());
    }

    private LocalDate extractDueDate(ImmunizationRecommendation resource) {
        if (!resource.hasRecommendation()) {
            return null;
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationComponent recommendation =
                resource.getRecommendationFirstRep();

        if (!recommendation.hasDateCriterion() || recommendation.getDateCriterion().isEmpty()) {
            return null;
        }

        ImmunizationRecommendation.ImmunizationRecommendationRecommendationDateCriterionComponent criterion =
                recommendation.getDateCriterionFirstRep();

        if (!criterion.hasValue()) {
            return null;
        }

        return support.toLocalDate(criterion.getValue());
    }

    private String extractRecommendationSource(ImmunizationRecommendation resource) {
        if (!resource.hasAuthority()) {
            return null;
        }

        Reference source = resource.getAuthority();

        if (source.hasDisplay()) {
            return source.getDisplay();
        }

        return source.hasReference() ? source.getReference() : null;
    }
}