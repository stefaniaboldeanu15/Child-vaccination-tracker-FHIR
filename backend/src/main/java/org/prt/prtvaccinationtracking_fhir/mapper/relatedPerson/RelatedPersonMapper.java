package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.UpdateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface RelatedPersonMapper {

    String IDENTIFIER_SYSTEM = "urn:system:related-person-identifier";

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "fullName", expression = "java(extractFullName(resource))")
    @Mapping(target = "identifier", expression = "java(extractIdentifier(resource))")
    @Mapping(target = "relationship", expression = "java(extractRelationship(resource))")
    @Mapping(target = "phone", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE))")
    @Mapping(target = "email", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL))")
    @Mapping(target = "address", expression = "java(extractAddress(resource))")
    @Mapping(target = "patientId", expression = "java(resource.hasPatient() ? resource.getPatient().getReferenceElement().getIdPart() : null)")
    RelatedPersonDTO toDTO(RelatedPerson resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifier", expression = "java(dto.identifier() != null ? toIdentifierList(dto.identifier()) : Collections.emptyList())")
    @Mapping(target = "name", expression = "java(toHumanName(dto.firstName(), dto.lastName()))")
    @Mapping(target = "relationship", expression = "java(dto.relationship() != null ? toRelationship(dto.relationship()) : Collections.emptyList())")
    @Mapping(target = "telecom", expression = "java(toTelecom(dto.phone(), dto.email()))")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : Collections.emptyList())")
    @Mapping(target = "patient", expression = "java(dto.patientId() != null ? new Reference(\"Patient/\" + dto.patientId()) : null)")
    RelatedPerson toResource(CreateRelatedPersonRequestDTO dto);

    @Mapping(target = "identifier", expression = "java(resource.getIdentifier())")
    @Mapping(target = "name", expression = "java((dto.firstName() != null || dto.lastName() != null) ? toHumanName(dto.firstName() != null ? dto.firstName() : extractGiven(resource), dto.lastName() != null ? dto.lastName() : extractFamily(resource)) : resource.getName())")
    @Mapping(target = "relationship", expression = "java(dto.relationship() != null ? toRelationship(dto.relationship()) : resource.getRelationship())")
    @Mapping(target = "telecom", expression = "java((dto.phone() != null || dto.email() != null) ? toTelecom(dto.phone() != null ? dto.phone() : extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE), dto.email() != null ? dto.email() : extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL)) : resource.getTelecom())")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : resource.getAddress())")
    @Mapping(target = "patient", expression = "java(resource.getPatient())")
    void updateResourceFromDTO(UpdateRelatedPersonRequestDTO dto, @MappingTarget RelatedPerson resource);

    default List<Identifier> toIdentifierList(String identifier) {
        Identifier id = new Identifier();
        id.setSystem(IDENTIFIER_SYSTEM);
        id.setValue(identifier);
        return Collections.singletonList(id);
    }

    default List<CodeableConcept> toRelationship(String relationshipText) {
        return Collections.singletonList(new CodeableConcept().setText(relationshipText));
    }

    default List<ContactPoint> toTelecom(String phone, String email) {
        java.util.ArrayList<ContactPoint> out = new java.util.ArrayList<>();
        if (phone != null) {
            ContactPoint cp = new ContactPoint();
            cp.setSystem(ContactPoint.ContactPointSystem.PHONE);
            cp.setValue(phone);
            out.add(cp);
        }
        if (email != null) {
            ContactPoint cp = new ContactPoint();
            cp.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            cp.setValue(email);
            out.add(cp);
        }
        return out;
    }

    default List<Address> toAddressList(String addressText) {
        return Collections.singletonList(new Address().setText(addressText));
    }

    default String extractIdentifier(RelatedPerson resource) {
        if (resource == null || !resource.hasIdentifier()) return null;
        for (Identifier id : resource.getIdentifier()) {
            if (IDENTIFIER_SYSTEM.equals(id.getSystem()) && id.hasValue()) return id.getValue();
        }
        return resource.getIdentifierFirstRep().hasValue() ? resource.getIdentifierFirstRep().getValue() : null;
    }

    default String extractGiven(RelatedPerson resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        if (!n.hasGiven() || n.getGiven().isEmpty()) return null;
        return n.getGiven().get(0).getValue();
    }

    default String extractFamily(RelatedPerson resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        return n.hasFamily() ? n.getFamily() : null;
    }

    default String extractFullName(RelatedPerson resource) {
        String g = extractGiven(resource);
        String f = extractFamily(resource);
        if (g == null && f == null) return null;
        if (g == null) return f;
        if (f == null) return g;
        return g + " " + f;
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


