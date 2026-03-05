package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.ExtendedContactDetail;
import org.hl7.fhir.r5.model.Location;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.CreateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.UpdateLocationRequestDTO;

import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface LocationMapper {

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "name", expression = "java(resource.getName())")
    @Mapping(target = "description", expression = "java(resource.getDescription())")
    @Mapping(target = "address", expression = "java(extractAddressText(resource))")
    @Mapping(target = "phone", expression = "java(extractPhone(resource))")
    @Mapping(target = "type", expression = "java(extractTypeText(resource))")
    LocationDTO toDTO(Location resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(dto.name())")
    @Mapping(target = "description", expression = "java(dto.description())")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddress(dto.address()) : null)")
    @Mapping(target = "contact", expression = "java(dto.phone() != null ? toPhoneContacts(dto.phone()) : Collections.emptyList())")
    @Mapping(target = "type", expression = "java(dto.type() != null ? toTypeList(dto.type()) : Collections.emptyList())")
    Location toResource(CreateLocationRequestDTO dto);

    @Mapping(target = "name", expression = "java(dto.name() != null ? dto.name() : resource.getName())")
    @Mapping(target = "description", expression = "java(dto.description() != null ? dto.description() : resource.getDescription())")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddress(dto.address()) : resource.getAddress())")
    @Mapping(target = "contact", expression = "java(dto.phone() != null ? toPhoneContacts(dto.phone()) : resource.getContact())")
    @Mapping(target = "type", expression = "java(dto.type() != null ? toTypeList(dto.type()) : resource.getType())")
    void updateResourceFromDTO(UpdateLocationRequestDTO dto, @MappingTarget Location resource);

    default Address toAddress(String addressText) {
        return addressText == null ? null : new Address().setText(addressText);
    }

    default List<ExtendedContactDetail> toPhoneContacts(String phone) {
        ContactPoint cp = new ContactPoint();
        cp.setSystem(ContactPoint.ContactPointSystem.PHONE);
        cp.setValue(phone);

        ExtendedContactDetail ecd = new ExtendedContactDetail();
        ecd.setTelecom(Collections.singletonList(cp));

        return Collections.singletonList(ecd);
    }

    default List<CodeableConcept> toTypeList(String typeText) {
        return Collections.singletonList(new CodeableConcept().setText(typeText));
    }

    default String extractAddressText(Location resource) {
        if (resource == null || !resource.hasAddress()) return null;
        Address a = resource.getAddress();
        return a.hasText() ? a.getText() : null;
    }

    default String extractPhone(Location resource) {
        if (resource == null || !resource.hasContact() || resource.getContact().isEmpty()) return null;
        for (ExtendedContactDetail c : resource.getContact()) {
            if (!c.hasTelecom()) continue;
            for (ContactPoint cp : c.getTelecom()) {
                if (cp.getSystem() == ContactPoint.ContactPointSystem.PHONE && cp.hasValue()) {
                    return cp.getValue();
                }
            }
        }
        return null;
    }

    default String extractTypeText(Location resource) {
        if (resource == null || !resource.hasType() || resource.getType().isEmpty()) return null;
        CodeableConcept cc = resource.getTypeFirstRep();
        return cc.hasText() ? cc.getText() : null;
    }
}