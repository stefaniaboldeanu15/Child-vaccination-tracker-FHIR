package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface CreateRelatedPersonRequestMapper {

    String IDENTIFIER_SYSTEM = "urn:system:related-person-identifier";

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifier", expression = "java(dto.relatedPersonIdentifier() != null ? toIdentifierList(dto.relatedPersonIdentifier()) : Collections.emptyList())")
    @Mapping(target = "name", expression = "java(dto.fullName() != null ? toHumanNameFromFullName(dto.fullName()) : Collections.emptyList())")
    @Mapping(target = "relationship", expression = "java(dto.relationship() != null ? toRelationship(dto.relationship()) : Collections.emptyList())")
    @Mapping(target = "telecom", expression = "java(toTelecom(dto.phone(), dto.email()))")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : Collections.emptyList())")
    @Mapping(target = "patient", expression = "java(dto.patientId() != null ? new Reference(\"Patient/\" + dto.patientId()) : null)")
    org.hl7.fhir.r5.model.RelatedPerson toResource(CreateRelatedPersonRequestDTO dto);

    default List<Identifier> toIdentifierList(String identifier) {
        Identifier id = new Identifier();
        id.setSystem(IDENTIFIER_SYSTEM);
        id.setValue(identifier);
        return Collections.singletonList(id);
    }

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
}

