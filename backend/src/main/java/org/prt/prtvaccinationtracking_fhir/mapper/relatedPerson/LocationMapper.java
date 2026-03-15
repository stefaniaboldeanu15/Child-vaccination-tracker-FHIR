package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.CreateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.UpdateLocationRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("relatedpersonLocationMapper")
public class LocationMapper {

    private final MapperSupport support;

    public LocationMapper(MapperSupport support) {
        this.support = support;
    }

    public LocationDTO toDTO(Location resource) {
        if (resource == null) {
            return null;
        }

        return new LocationDTO(
                resource.getIdElement().getIdPart(),
                resource.hasName() ? resource.getName() : null,
                resource.hasDescription() ? resource.getDescription() : null,
                extractAddress(resource),
                extractPhone(resource),
                extractType(resource)
        );
    }

    public Location toResource(CreateLocationRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Location resource = new Location();

        if (dto.name() != null) {
            resource.setName(dto.name());
        }

        if (dto.description() != null) {
            resource.setDescription(dto.description());
        }

        if (dto.address() != null && !dto.address().isBlank()) {
            resource.setAddress(new Address().setText(dto.address()));
        }

        if (dto.phone() != null && !dto.phone().isBlank()) {
            resource.setContact(List.of(
                    new ExtendedContactDetail().setTelecom(List.of(
                            new ContactPoint()
                                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                                    .setValue(dto.phone())
                    ))
            ));
        }

        if (dto.type() != null && !dto.type().isBlank()) {
            resource.setType(List.of(new CodeableConcept().setText(dto.type())));
        }

        return resource;
    }

    public void updateResource(UpdateLocationRequestDTO dto, Location resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.name() != null) {
            resource.setName(dto.name());
        }

        if (dto.description() != null) {
            resource.setDescription(dto.description());
        }

        if (dto.address() != null) {
            if (dto.address().isBlank()) {
                resource.setAddress(null);
            } else {
                resource.setAddress(new Address().setText(dto.address()));
            }
        }

        if (dto.phone() != null) {
            if (dto.phone().isBlank()) {
                resource.setContact(new ArrayList<>());
            } else {
                resource.setContact(List.of(
                        new ExtendedContactDetail().setTelecom(List.of(
                                new ContactPoint()
                                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                                        .setValue(dto.phone())
                        ))
                ));
            }
        }

        if (dto.type() != null) {
            if (dto.type().isBlank()) {
                resource.setType(new ArrayList<>());
            } else {
                resource.setType(List.of(new CodeableConcept().setText(dto.type())));
            }
        }
    }

    private String extractAddress(Location resource) {
        if (!resource.hasAddress()) {
            return null;
        }

        Address address = resource.getAddress();
        return address.hasText() ? address.getText() : null;
    }

    private String extractPhone(Location resource) {
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

    private String extractType(Location resource) {
        if (!resource.hasType() || resource.getType().isEmpty()) {
            return null;
        }

        return support.codeableConceptToText(resource.getTypeFirstRep());
    }
}