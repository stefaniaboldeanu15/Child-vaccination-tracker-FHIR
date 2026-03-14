package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface RelatedPersonDtoMapper {

    String IDENTIFIER_SYSTEM = "urn:system:related-person-identifier";

    // FHIR → DTO (guardian overview)
    @Mapping(target = "relatedPersonId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "relatedPersonIdentifier", expression = "java(extractIdentifier(resource))")
    @Mapping(target = "relationship", expression = "java(extractRelationship(resource))")
    @Mapping(target = "fullName", expression = "java(extractFullName(resource))")
    @Mapping(target = "phone", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE))")
    @Mapping(target = "email", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL))")
    @Mapping(target = "address", expression = "java(extractAddress(resource))")
    RelatedPersonDTO toDTO(RelatedPerson resource);

    default String extractIdentifier(RelatedPerson resource) {
        if (resource == null || !resource.hasIdentifier()) return null;
        for (Identifier id : resource.getIdentifier()) {
            if (IDENTIFIER_SYSTEM.equals(id.getSystem()) && id.hasValue()) return id.getValue();
        }
        return resource.getIdentifierFirstRep().hasValue() ? resource.getIdentifierFirstRep().getValue() : null;
    }

    default String extractFullName(RelatedPerson resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        String given = (n.hasGiven() && !n.getGiven().isEmpty()) ? n.getGiven().get(0).getValue() : null;
        String family = n.hasFamily() ? n.getFamily() : null;
        if (given == null && family == null) return null;
        if (given == null) return family;
        if (family == null) return given;
        return given + " " + family;
    }

    default String extractRelationship(RelatedPerson resource) {
        if (resource == null || !resource.hasRelationship() || resource.getRelationship().isEmpty()) return null;
        CodeableConcept cc = resource.getRelationshipFirstRep();
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractTelecom(RelatedPerson resource, ContactPoint.ContactPointSystem system) {
        if (resource == null || !resource.hasTelecom()) return null;
        for (ContactPoint cp : resource.getTelecom()) {
            if (cp.getSystem() == system && cp.hasValue()) return cp.getValue();
        }
        return null;
    }

    default String extractAddress(RelatedPerson resource) {
        if (resource == null || !resource.hasAddress() || resource.getAddress().isEmpty()) return null;
        Address addr = resource.getAddressFirstRep();
        return addr.hasText() ? addr.getText() : null;
    }
}

