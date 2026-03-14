package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.CreateImmunizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.ImmunizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.ImmunizationStatusDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.UpdateImmunizationRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component("relatedpersonImmunizationMapper")
public class ImmunizationMapper {

    private final MapperSupport support;

    public ImmunizationMapper(MapperSupport support) {
        this.support = support;
    }

    public ImmunizationDTO toDTO(Immunization resource) {
        if (resource == null) {
            return null;
        }

        return new ImmunizationDTO(
                resource.getIdElement().getIdPart(),
                resource.hasPatient() ? support.referenceToId(resource.getPatient()) : null,
                extractVaccineCode(resource),
                extractVaccineDisplay(resource),
                extractAdministrationDate(resource),
                extractDoseNumber(resource),
                resource.hasLotNumber() ? resource.getLotNumber() : null,
                resource.hasSite() ? support.codeableConceptToText(resource.getSite()) : null,
                resource.hasStatus() ? ImmunizationStatusDTO.fromFhirCode(resource.getStatus().toCode()) : null,
                extractPractitionerName(resource),
                resource.hasEncounter() ? support.referenceToId(resource.getEncounter()) : null,
                resource.hasReaction() && !resource.getReaction().isEmpty()
        );
    }

    public Immunization toResource(CreateImmunizationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Immunization resource = new Immunization();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setPatient(support.toPatientReference(dto.patientId()));
        }

        if (dto.status() != null) {
            resource.setStatus(Immunization.ImmunizationStatusCodes.fromCode(dto.status().toFhirCode()));
        }

        if (dto.vaccineCode() != null || dto.vaccineDisplay() != null) {
            resource.setVaccineCode(toVaccineCode(dto.vaccineCode(), dto.vaccineDisplay()));
        }

        if (dto.administrationDate() != null) {
            resource.setOccurrence(new DateTimeType(support.toDate(dto.administrationDate())));
        }

        if (dto.lotNumber() != null && !dto.lotNumber().isBlank()) {
            resource.setLotNumber(dto.lotNumber());
        }

        if (dto.site() != null && !dto.site().isBlank()) {
            resource.setSite(new CodeableConcept().setText(dto.site()));
        }

        if (dto.doseNumber() != null) {
            resource.setProtocolApplied(List.of(
                    new Immunization.ImmunizationProtocolAppliedComponent()
                            .setDoseNumber(String.valueOf(dto.doseNumber()))
            ));
        }

        if (dto.encounterId() != null && !dto.encounterId().isBlank()) {
            resource.setEncounter(support.toEncounterReference(dto.encounterId()));
        }

        return resource;
    }

    public void updateResource(UpdateImmunizationRequestDTO dto, Immunization resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.lotNumber() != null) {
            resource.setLotNumber(dto.lotNumber());
        }

        if (dto.site() != null) {
            if (dto.site().isBlank()) {
                resource.setSite(null);
            } else {
                resource.setSite(new CodeableConcept().setText(dto.site()));
            }
        }

        if (dto.status() != null) {
            resource.setStatus(Immunization.ImmunizationStatusCodes.fromCode(dto.status().toFhirCode()));
        }

        if (dto.notes() != null) {
            if (dto.notes().isBlank()) {
                resource.setNote(new ArrayList<>());
            } else {
                resource.setNote(List.of(new Annotation().setText(dto.notes())));
            }
        }
    }

    private CodeableConcept toVaccineCode(String code, String display) {
        CodeableConcept concept = new CodeableConcept();

        if (display != null && !display.isBlank()) {
            concept.setText(display);
        }

        if (code != null && !code.isBlank()) {
            concept.addCoding().setCode(code).setDisplay(display);
        }

        return concept;
    }

    private String extractVaccineCode(Immunization resource) {
        if (!resource.hasVaccineCode() || !resource.getVaccineCode().hasCoding()) {
            return null;
        }

        return resource.getVaccineCode().getCodingFirstRep().hasCode()
                ? resource.getVaccineCode().getCodingFirstRep().getCode()
                : null;
    }

    private String extractVaccineDisplay(Immunization resource) {
        if (!resource.hasVaccineCode()) {
            return null;
        }

        if (resource.getVaccineCode().hasCoding() && resource.getVaccineCode().getCodingFirstRep().hasDisplay()) {
            return resource.getVaccineCode().getCodingFirstRep().getDisplay();
        }

        return resource.getVaccineCode().hasText() ? resource.getVaccineCode().getText() : null;
    }

    private LocalDate extractAdministrationDate(Immunization resource) {
        if (!resource.hasOccurrenceDateTimeType()) {
            return null;
        }

        return support.toLocalDate(resource.getOccurrenceDateTimeType().getValue());
    }

    private Integer extractDoseNumber(Immunization resource) {
        if (!resource.hasProtocolApplied() || resource.getProtocolApplied().isEmpty()) {
            return null;
        }

        String doseNumber = resource.getProtocolAppliedFirstRep().getDoseNumber();
        if (doseNumber == null || doseNumber.isBlank()) {
            return null;
        }

        try {
            return Integer.parseInt(doseNumber);
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private String extractPractitionerName(Immunization resource) {
        if (!resource.hasPerformer() || resource.getPerformer().isEmpty()) {
            return null;
        }

        for (Immunization.ImmunizationPerformerComponent performer : resource.getPerformer()) {
            if (performer == null || !performer.hasActor()) {
                continue;
            }

            Reference actor = performer.getActor();

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

            if (actor.hasReference()) {
                return support.referenceToId(actor);
            }
        }

        return null;
    }
}