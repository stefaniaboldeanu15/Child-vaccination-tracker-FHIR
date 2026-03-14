package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.CreateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.OrganizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.UpdateOrganizationRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class OrganizationMapper {

    private final MapperSupport support;

    public OrganizationMapper(MapperSupport support) {
        this.support = support;
    }

    public OrganizationDTO toDTO(Organization resource) {
        if (resource == null) {
            return null;
        }

        return new OrganizationDTO(
                resource.getIdElement().getIdPart(),
                resource.hasName() ? resource.getName() : null,
                extractType(resource),
                extractPhone(resource),
                extractAddress(resource)
        );
    }

    public Organization toResource(CreateOrganizationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Organization resource = new Organization();

        if (dto.name() != null) {
            resource.setName(dto.name());
        }

        if (dto.type() != null && !dto.type().isBlank()) {
            resource.setType(List.of(new CodeableConcept().setText(dto.type())));
        }

        if (dto.phone() != null || dto.email() != null || dto.address() != null) {
            ExtendedContactDetail contact = buildContact(dto.phone(), dto.email(), dto.address());
            if (contact != null) {
                resource.setContact(List.of(contact));
            }
        }

        return resource;
    }

    public void updateResource(UpdateOrganizationRequestDTO dto, Organization resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.name() != null) {
            resource.setName(dto.name());
        }

        if (dto.phone() != null || dto.email() != null || dto.address() != null) {
            String currentPhone = extractPhone(resource);
            String currentEmail = extractEmail(resource);
            String currentAddress = extractAddress(resource);

            String phone = dto.phone() != null ? dto.phone() : currentPhone;
            String email = dto.email() != null ? dto.email() : currentEmail;
            String address = dto.address() != null ? dto.address() : currentAddress;

            if (isBlank(phone) && isBlank(email) && isBlank(address)) {
                resource.setContact(new ArrayList<>());
            } else {
                ExtendedContactDetail contact = buildContact(phone, email, address);
                resource.setContact(contact == null ? new ArrayList<>() : List.of(contact));
            }
        }
    }

    private ExtendedContactDetail buildContact(String phone, String email, String addressText) {
        List<ContactPoint> telecom = new ArrayList<>();

        if (!isBlank(phone)) {
            telecom.add(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(phone));
        }

        if (!isBlank(email)) {
            telecom.add(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(email));
        }

        Address address = null;
        if (!isBlank(addressText)) {
            address = new Address().setText(addressText);
        }

        if (telecom.isEmpty() && address == null) {
            return null;
        }

        ExtendedContactDetail contact = new ExtendedContactDetail();

        if (!telecom.isEmpty()) {
            contact.setTelecom(telecom);
        }

        if (address != null) {
            contact.setAddress(address);
        }

        return contact;
    }

    private String extractType(Organization resource) {
        if (!resource.hasType() || resource.getType().isEmpty()) {
            return null;
        }

        return support.codeableConceptToText(resource.getTypeFirstRep());
    }

    private String extractPhone(Organization resource) {
        if (!resource.hasContact() || resource.getContact().isEmpty()) {
            return null;
        }

        for (ExtendedContactDetail contact : resource.getContact()) {
            if (contact == null || !contact.hasTelecom() || contact.getTelecom().isEmpty()) {
                continue;
            }

            for (ContactPoint telecom : contact.getTelecom()) {
                if (telecom != null
                        && telecom.hasSystem()
                        && telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE
                        && telecom.hasValue()) {
                    return telecom.getValue();
                }
            }
        }

        return null;
    }

    private String extractEmail(Organization resource) {
        if (!resource.hasContact() || resource.getContact().isEmpty()) {
            return null;
        }

        for (ExtendedContactDetail contact : resource.getContact()) {
            if (contact == null || !contact.hasTelecom() || contact.getTelecom().isEmpty()) {
                continue;
            }

            for (ContactPoint telecom : contact.getTelecom()) {
                if (telecom != null
                        && telecom.hasSystem()
                        && telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL
                        && telecom.hasValue()) {
                    return telecom.getValue();
                }
            }
        }

        return null;
    }

    private String extractAddress(Organization resource) {
        if (!resource.hasContact() || resource.getContact().isEmpty()) {
            return null;
        }

        for (ExtendedContactDetail contact : resource.getContact()) {
            if (contact != null && contact.hasAddress() && contact.getAddress().hasText()) {
                return contact.getAddress().getText();
            }
        }

        return null;
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}