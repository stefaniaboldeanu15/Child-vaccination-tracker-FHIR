package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.Organization;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.CreateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.OrganizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.UpdateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface OrganizationMapper {

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "name", expression = "java(resource.getName())")
    @Mapping(target = "type", expression = "java(extractTypeText(resource))")
    @Mapping(target = "phone", expression = "java(extractTelecomValue(resource, \"PHONE\"))")
    @Mapping(target = "email", expression = "java(extractTelecomValue(resource, \"EMAIL\"))")
    @Mapping(target = "address", expression = "java(extractAddressText(resource))")
    OrganizationDTO toDTO(Organization resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "name", expression = "java(dto.name())")
    @Mapping(target = "type", expression = "java(dto.type() != null ? toTypeList(dto.type()) : Collections.emptyList())")
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "telecom", ignore = true)
    Organization toResource(CreateOrganizationRequestDTO dto);

    @Mapping(target = "name", expression = "java(dto.name() != null ? dto.name() : resource.getName())")
    @Mapping(target = "type", expression = "java(dto.type() != null ? toTypeList(dto.type()) : resource.getType())")
    @Mapping(target = "address", ignore = true)
    @Mapping(target = "telecom", ignore = true)
    void updateResourceFromDTO(UpdateOrganizationRequestDTO dto, @MappingTarget Organization resource);

    @AfterMapping
    default void afterCreate(CreateOrganizationRequestDTO dto, @MappingTarget Organization resource) {
        if (dto.address() != null) setAddressReflective(resource, toAddressList(dto.address()));
        setTelecomReflective(resource, toTelecomList(dto.phone(), dto.email()));
    }

    @AfterMapping
    default void afterUpdate(UpdateOrganizationRequestDTO dto, @MappingTarget Organization resource) {
        if (dto.address() != null) setAddressReflective(resource, toAddressList(dto.address()));
        if (dto.phone() != null || dto.email() != null) {
            List<ContactPoint> merged = mergeTelecomReflective(resource, dto.phone(), dto.email());
            setTelecomReflective(resource, merged);
        }
    }

    default List<CodeableConcept> toTypeList(String typeText) {
        if (typeText == null) return Collections.emptyList();
        return Collections.singletonList(new CodeableConcept().setText(typeText));
    }

    default String extractTypeText(Organization resource) {
        if (resource == null) return null;
        try {
            Method m = resource.getClass().getMethod("getType");
            Object v = m.invoke(resource);
            if (!(v instanceof List<?> list) || list.isEmpty()) return null;
            Object first = list.get(0);
            if (!(first instanceof CodeableConcept cc)) return null;
            return cc.hasText() ? cc.getText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    default List<Address> toAddressList(String addressText) {
        if (addressText == null) return Collections.emptyList();
        return Collections.singletonList(new Address().setText(addressText));
    }

    default String extractAddressText(Organization resource) {
        if (resource == null) return null;
        try {
            Method m = resource.getClass().getMethod("getAddress");
            Object v = m.invoke(resource);
            if (!(v instanceof List<?> list) || list.isEmpty()) return null;
            Object first = list.get(0);
            if (!(first instanceof Address a)) return null;
            return a.hasText() ? a.getText() : null;
        } catch (Exception e) {
            return null;
        }
    }

    default List<ContactPoint> toTelecomList(String phone, String email) {
        List<ContactPoint> list = new ArrayList<>();
        if (phone != null) {
            ContactPoint cp = new ContactPoint();
            cp.setSystem(ContactPoint.ContactPointSystem.PHONE);
            cp.setValue(phone);
            list.add(cp);
        }
        if (email != null) {
            ContactPoint cp = new ContactPoint();
            cp.setSystem(ContactPoint.ContactPointSystem.EMAIL);
            cp.setValue(email);
            list.add(cp);
        }
        return list;
    }

    default String extractTelecomValue(Organization resource, String systemName) {
        if (resource == null) return null;
        List<ContactPoint> telecom = getTelecomReflective(resource);
        if (telecom == null) return null;
        for (ContactPoint cp : telecom) {
            if (cp != null && cp.hasSystem() && cp.getSystem() != null && cp.getSystem().name().equals(systemName) && cp.hasValue()) {
                return cp.getValue();
            }
        }
        return null;
    }

    default List<ContactPoint> mergeTelecomReflective(Organization resource, String phone, String email) {
        List<ContactPoint> existing = getTelecomReflective(resource);
        List<ContactPoint> result = new ArrayList<>();
        if (existing != null) result.addAll(existing);

        if (phone != null) {
            boolean updated = false;
            for (ContactPoint cp : result) {
                if (cp != null && cp.hasSystem() && cp.getSystem() == ContactPoint.ContactPointSystem.PHONE) {
                    cp.setValue(phone);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                ContactPoint cp = new ContactPoint();
                cp.setSystem(ContactPoint.ContactPointSystem.PHONE);
                cp.setValue(phone);
                result.add(cp);
            }
        }

        if (email != null) {
            boolean updated = false;
            for (ContactPoint cp : result) {
                if (cp != null && cp.hasSystem() && cp.getSystem() == ContactPoint.ContactPointSystem.EMAIL) {
                    cp.setValue(email);
                    updated = true;
                    break;
                }
            }
            if (!updated) {
                ContactPoint cp = new ContactPoint();
                cp.setSystem(ContactPoint.ContactPointSystem.EMAIL);
                cp.setValue(email);
                result.add(cp);
            }
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    default List<ContactPoint> getTelecomReflective(Organization resource) {
        try {
            Method m = resource.getClass().getMethod("getTelecom");
            Object v = m.invoke(resource);
            if (v instanceof List<?> list) return (List<ContactPoint>) list;
            return null;
        } catch (Exception e) {
            return null;
        }
    }

    default void setTelecomReflective(Organization resource, List<ContactPoint> telecom) {
        try {
            Method m = resource.getClass().getMethod("setTelecom", List.class);
            m.invoke(resource, telecom);
        } catch (Exception ignored) {
        }
    }

    default void setAddressReflective(Organization resource, List<Address> address) {
        try {
            Method m = resource.getClass().getMethod("setAddress", List.class);
            m.invoke(resource, address);
        } catch (Exception ignored) {
        }
    }
}