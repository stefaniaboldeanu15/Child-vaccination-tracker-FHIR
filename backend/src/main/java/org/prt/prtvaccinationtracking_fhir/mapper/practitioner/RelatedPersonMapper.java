package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.UpdateRelatedPersonRequestDTO;

import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface RelatedPersonMapper {

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "fullName", expression = "java(extractFullName(resource))")
    @Mapping(target = "relationship", expression = "java(extractRelationship(resource))")
    @Mapping(target = "phone", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE))")
    @Mapping(target = "email", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL))")
    RelatedPersonDTO toDTO(RelatedPerson resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "patient", expression = "java(new Reference(\"Patient/\" + patientId))")
    @Mapping(target = "name", expression = "java(toHumanName(dto.firstName(), dto.lastName()))")
    @Mapping(target = "relationship", expression = "java(dto.relationship() != null ? toRelationship(dto.relationship()) : Collections.emptyList())")
    @Mapping(target = "telecom", expression = "java(toTelecom(dto.phone(), dto.email()))")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : Collections.emptyList())")
    RelatedPerson toResource(CreateRelatedPersonRequestDTO dto, String patientId);

    @Mapping(target = "telecom", expression = "java((dto.phone() != null || dto.email() != null) ? toTelecom(dto.phone() != null ? dto.phone() : extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE), dto.email() != null ? dto.email() : extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL)) : resource.getTelecom())")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : resource.getAddress())")
    @Mapping(target = "patient", expression = "java(resource.getPatient())")
    @Mapping(target = "name", expression = "java(resource.getName())")
    @Mapping(target = "relationship", expression = "java(resource.getRelationship())")
    void updateResourceFromDTO(UpdateRelatedPersonRequestDTO dto, @MappingTarget RelatedPerson resource);

    default List<HumanName> toHumanName(String firstName, String lastName) {
        HumanName hn = new HumanName();
        if (lastName != null) hn.setFamily(lastName);
        if (firstName != null) hn.setGiven(Collections.singletonList(new StringType(firstName)));
        return Collections.singletonList(hn);
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
}