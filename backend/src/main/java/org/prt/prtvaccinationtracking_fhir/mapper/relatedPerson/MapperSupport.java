package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Component("relatedpersonSupportMapper")
public class MapperSupport {

    public String referenceToId(Reference reference) {
        if (reference == null || !reference.hasReferenceElement()) {
            return null;
        }
        return reference.getReferenceElement().getIdPart();
    }

    public Reference toPatientReference(String patientId) {
        return buildReference("Patient", patientId);
    }

    public Reference toEncounterReference(String encounterId) {
        return buildReference("Encounter", encounterId);
    }

    public Reference toPractitionerReference(String practitionerId) {
        return buildReference("Practitioner", practitionerId);
    }

    public Reference toOrganizationReference(String organizationId) {
        return buildReference("Organization", organizationId);
    }

    public Reference toLocationReference(String locationId) {
        return buildReference("Location", locationId);
    }

    public Reference toGoalReference(String goalId) {
        return buildReference("Goal", goalId);
    }

    public Reference toImmunizationReference(String immunizationId) {
        return buildReference("Immunization", immunizationId);
    }

    public Reference toImmunizationRecommendationReference(String recommendationId) {
        return buildReference("ImmunizationRecommendation", recommendationId);
    }

    public Reference toRelatedPersonReference(String relatedPersonId) {
        return buildReference("RelatedPerson", relatedPersonId);
    }

    private Reference buildReference(String resourceType, String id) {
        if (id == null || id.isBlank()) {
            return null;
        }
        return new Reference(resourceType + "/" + id);
    }

    public Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return Instant.ofEpochMilli(date.getTime())
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    public CodeableConcept toCodeableConcept(String text) {
        if (text == null || text.isBlank()) {
            return null;
        }
        return new CodeableConcept().setText(text);
    }

    public String codeableConceptToText(CodeableConcept concept) {
        if (concept == null) {
            return null;
        }
        if (concept.hasText()) {
            return concept.getText();
        }
        if (concept.hasCoding() && concept.getCodingFirstRep().hasDisplay()) {
            return concept.getCodingFirstRep().getDisplay();
        }
        if (concept.hasCoding() && concept.getCodingFirstRep().hasCode()) {
            return concept.getCodingFirstRep().getCode();
        }
        return null;
    }

    public List<Communication.CommunicationPayloadComponent> toPayload(String message) {
        if (message == null || message.isBlank()) {
            return List.of();
        }

        Communication.CommunicationPayloadComponent payload =
                new Communication.CommunicationPayloadComponent();
        payload.setContent(new StringType(message));
        return List.of(payload);
    }

    public String payloadToMessage(List<Communication.CommunicationPayloadComponent> payload) {
        if (payload == null || payload.isEmpty()) {
            return null;
        }

        Communication.CommunicationPayloadComponent first = payload.get(0);
        if (first == null || !first.hasContent()) {
            return null;
        }

        DataType content = first.getContent();
        if (content instanceof StringType stringType) {
            return stringType.getValue();
        }

        return null;
    }
}