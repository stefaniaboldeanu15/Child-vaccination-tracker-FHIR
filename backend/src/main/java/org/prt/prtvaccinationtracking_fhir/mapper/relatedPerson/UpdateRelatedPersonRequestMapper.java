package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.UpdateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface UpdateRelatedPersonRequestMapper {

    // UPDATE DTO → existing FHIR RelatedPerson
    @Mapping(target = "identifier", expression = "java(resource.getIdentifier())")
    @Mapping(target = "name", expression = "java(dto.fullName() != null ? toHumanNameFromFullName(dto.fullName()) : resource.getName())")
    @Mapping(target = "relationship", expression = "java(dto.relationship() != null ? toRelationship(dto.relationship()) : resource.getRelationship())")
    @Mapping(target = "telecom", expression = "java((dto.phone() != null || dto.email() != null) ? toTelecom(dto.phone() != null ? dto.phone() : extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE), dto.email() != null ? dto.email() : extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL)) : resource.getTelecom())")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : resource.getAddress())")
    @Mapping(target = "patient", expression = "java(resource.getPatient())")
    void updateResourceFromDTO(UpdateRelatedPersonRequestDTO dto,
                               @MappingTarget org.hl7.fhir.r5.model.RelatedPerson resource);

    default List<HumanName> toHumanNameFromFullName(String fullName) {
        HumanName hn = new HumanName();
        hn.setText(fullName);
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

    default String extractTelecom(RelatedPerson resource, ContactPoint.ContactPointSystem system) {
        if (resource == null || !resource.hasTelecom()) return null;
        for (ContactPoint cp : resource.getTelecom()) {
            if (cp.getSystem() == system && cp.hasValue()) return cp.getValue();
        }
        return null;
    }
}

