package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Consent;
import org.hl7.fhir.r5.model.Reference;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent.ConsentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent.CreateConsentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent.UpdateConsentRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component("relatedpersonConsentMapper")
public class ConsentMapper {

    private final MapperSupport support;

    public ConsentMapper(MapperSupport support) {
        this.support = support;
    }

    public ConsentDTO toDTO(Consent resource) {
        if (resource == null) {
            return null;
        }

        return new ConsentDTO(
                resource.getIdElement().getIdPart(),
                resource.hasSubject() ? support.referenceToId(resource.getSubject()) : null,
                extractRelatedPersonId(resource),
                extractScope(resource),
                resource.hasStatus() ? resource.getStatus().toCode() : null,
                extractDateGiven(resource)
        );
    }

    public Consent toResource(CreateConsentRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Consent resource = new Consent();

        if (dto.patientId() != null && !dto.patientId().isBlank()) {
            resource.setSubject(support.toPatientReference(dto.patientId()));
        }

        if (dto.relatedPersonId() != null && !dto.relatedPersonId().isBlank()) {
            resource.setGrantee(List.of(support.toRelatedPersonReference(dto.relatedPersonId())));
        }

        if (dto.scope() != null && !dto.scope().isBlank()) {
            resource.setCategory(List.of(new CodeableConcept().setText(dto.scope())));
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Consent.ConsentState.fromCode(dto.status()));
        }

        if (dto.dateGiven() != null) {
            resource.setDate(support.toDate(dto.dateGiven()));
        }

        return resource;
    }

    public void updateResource(UpdateConsentRequestDTO dto, Consent resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.status() != null && !dto.status().isBlank()) {
            resource.setStatus(Consent.ConsentState.fromCode(dto.status()));
        }
    }

    private String extractRelatedPersonId(Consent resource) {
        if (!resource.hasGrantee() || resource.getGrantee().isEmpty()) {
            return null;
        }

        for (Reference grantee : resource.getGrantee()) {
            if (grantee != null
                    && grantee.hasReference()
                    && grantee.getReference().startsWith("RelatedPerson/")) {
                return support.referenceToId(grantee);
            }
        }

        return null;
    }

    private String extractScope(Consent resource) {
        if (!resource.hasCategory() || resource.getCategory().isEmpty()) {
            return null;
        }

        return support.codeableConceptToText(resource.getCategoryFirstRep());
    }

    private LocalDate extractDateGiven(Consent resource) {
        if (!resource.hasDate()) {
            return null;
        }

        return support.toLocalDate(resource.getDate());
    }
}