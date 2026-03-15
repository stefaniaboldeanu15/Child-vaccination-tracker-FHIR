package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.ConditionDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.CreateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.UpdateConditionRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("relatedpersonConditionMapper")
public class ConditionMapper {

    private static final String CLINICAL_STATUS_SYSTEM =
            "https://terminology.hl7.org/CodeSystem/condition-clinical";

    private static final String VERIFICATION_STATUS_SYSTEM =
            "https://terminology.hl7.org/CodeSystem/condition-ver-status";

    private final MapperSupport support;

    public ConditionMapper(MapperSupport support) {
        this.support = support;
    }

    public ConditionDTO toDTO(Condition resource) {
        if (resource == null) {
            return null;
        }

        return new ConditionDTO(
                resource.getIdElement().getIdPart(),
                resource.hasSubject() ? support.referenceToId(resource.getSubject()) : null,
                extractCode(resource),
                extractDisplay(resource),
                extractClinicalStatus(resource),
                extractVerificationStatus(resource),
                extractOnsetDate(resource),
                null,
                extractNotes(resource)
        );
    }

    public Condition toResource(CreateConditionRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Condition resource = new Condition();

        if (dto.code() != null || dto.display() != null) {
            resource.setCode(toConditionCode(dto.code(), dto.display()));
        }

        if (dto.clinicalStatus() != null && !dto.clinicalStatus().isBlank()) {
            resource.setClinicalStatus(toClinicalStatus(dto.clinicalStatus()));
        }

        if (dto.verificationStatus() != null && !dto.verificationStatus().isBlank()) {
            resource.setVerificationStatus(toVerificationStatus(dto.verificationStatus()));
        }

        if (dto.onsetDate() != null) {
            resource.setOnset(new DateTimeType(support.toDate(dto.onsetDate())));
        }

        if (dto.notes() != null && !dto.notes().isBlank()) {
            resource.setNote(List.of(new Annotation().setText(dto.notes())));
        }

        return resource;
    }

    public void updateResource(UpdateConditionRequestDTO dto, Condition resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.clinicalStatus() != null) {
            if (dto.clinicalStatus().isBlank()) {
                resource.setClinicalStatus(null);
            } else {
                resource.setClinicalStatus(toClinicalStatus(dto.clinicalStatus()));
            }
        }

        if (dto.verificationStatus() != null) {
            if (dto.verificationStatus().isBlank()) {
                resource.setVerificationStatus(null);
            } else {
                resource.setVerificationStatus(toVerificationStatus(dto.verificationStatus()));
            }
        }

        if (dto.notes() != null) {
            if (dto.notes().isBlank()) {
                resource.setNote(new ArrayList<>());
            } else {
                resource.setNote(List.of(new Annotation().setText(dto.notes())));
            }
        }
    }

    private String extractCode(Condition resource) {
        if (!resource.hasCode() || !resource.getCode().hasCoding()) {
            return null;
        }

        Coding coding = resource.getCode().getCodingFirstRep();
        return coding.hasCode() ? coding.getCode() : null;
    }

    private String extractDisplay(Condition resource) {
        if (!resource.hasCode()) {
            return null;
        }

        CodeableConcept code = resource.getCode();

        if (code.hasCoding() && code.getCodingFirstRep().hasDisplay()) {
            return code.getCodingFirstRep().getDisplay();
        }

        return code.hasText() ? code.getText() : null;
    }

    private String extractClinicalStatus(Condition resource) {
        if (!resource.hasClinicalStatus()) {
            return null;
        }

        CodeableConcept concept = resource.getClinicalStatus();

        if (concept.hasCoding() && concept.getCodingFirstRep().hasCode()) {
            return concept.getCodingFirstRep().getCode();
        }

        return concept.hasText() ? concept.getText() : null;
    }

    private String extractVerificationStatus(Condition resource) {
        if (!resource.hasVerificationStatus()) {
            return null;
        }

        CodeableConcept concept = resource.getVerificationStatus();

        if (concept.hasCoding() && concept.getCodingFirstRep().hasCode()) {
            return concept.getCodingFirstRep().getCode();
        }

        return concept.hasText() ? concept.getText() : null;
    }

    private LocalDate extractOnsetDate(Condition resource) {
        if (!resource.hasOnsetDateTimeType()) {
            return null;
        }

        return support.toLocalDate(resource.getOnsetDateTimeType().getValue());
    }

    private String extractNotes(Condition resource) {
        if (!resource.hasNote() || resource.getNote().isEmpty()) {
            return null;
        }

        Annotation note = resource.getNoteFirstRep();
        return note.hasText() ? note.getText() : null;
    }

    private CodeableConcept toConditionCode(String code, String display) {
        CodeableConcept concept = new CodeableConcept();

        if (code != null && !code.isBlank()) {
            concept.addCoding(new Coding().setCode(code).setDisplay(display));
        }

        if (display != null && !display.isBlank()) {
            concept.setText(display);
        }

        return concept;
    }

    private CodeableConcept toClinicalStatus(String status) {
        String normalized = status.trim().toLowerCase();

        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(CLINICAL_STATUS_SYSTEM)
                        .setCode(normalized)
                        .setDisplay(normalized))
                .setText(normalized);
    }

    private CodeableConcept toVerificationStatus(String status) {
        String normalized = status.trim().toLowerCase();

        return new CodeableConcept()
                .addCoding(new Coding()
                        .setSystem(VERIFICATION_STATUS_SYSTEM)
                        .setCode(normalized)
                        .setDisplay(normalized))
                .setText(normalized);
    }
}