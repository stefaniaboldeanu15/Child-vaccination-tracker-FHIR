package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.UpdateRelatedPersonRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RelatedPersonRelatedPersonMapper {

    private final RelatedPersonMapperSupport support;

    public RelatedPersonRelatedPersonMapper(RelatedPersonMapperSupport support) {
        this.support = support;
    }

    public RelatedPersonDTO toDTO(RelatedPerson resource) {
        if (resource == null) {
            return null;
        }

        return new RelatedPersonDTO(
                resource.getIdElement().getIdPart(),
                extractFullName(resource),
                extractRelationship(resource),
                extractPhone(resource),
                extractEmail(resource)
        );
    }

    public RelatedPerson toResource(CreateRelatedPersonRequestDTO dto, String patientId) {
        if (dto == null) {
            return null;
        }

        RelatedPerson resource = new RelatedPerson();

        if (patientId != null && !patientId.isBlank()) {
            resource.setPatient(support.toPatientReference(patientId));
        }

        if (dto.firstName() != null || dto.lastName() != null) {
            resource.setName(List.of(buildHumanName(dto.firstName(), dto.lastName())));
        }

        if (dto.relationship() != null && !dto.relationship().isBlank()) {
            resource.setRelationship(List.of(new CodeableConcept().setText(dto.relationship())));
        }

        List<ContactPoint> telecom = buildTelecom(dto.phone(), dto.email());
        if (!telecom.isEmpty()) {
            resource.setTelecom(telecom);
        }

        if (dto.address() != null && !dto.address().isBlank()) {
            resource.setAddress(List.of(new Address().setText(dto.address())));
        }

        return resource;
    }

    public void updateResource(UpdateRelatedPersonRequestDTO dto, RelatedPerson resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.phone() != null || dto.email() != null) {
            String currentPhone = extractPhone(resource);
            String currentEmail = extractEmail(resource);

            String phone = dto.phone() != null ? dto.phone() : currentPhone;
            String email = dto.email() != null ? dto.email() : currentEmail;

            resource.setTelecom(buildTelecom(phone, email));
        }

        if (dto.address() != null) {
            if (dto.address().isBlank()) {
                resource.setAddress(new ArrayList<>());
            } else {
                resource.setAddress(List.of(new Address().setText(dto.address())));
            }
        }
    }

    private HumanName buildHumanName(String firstName, String lastName) {
        HumanName name = new HumanName();

        if (firstName != null && !firstName.isBlank()) {
            name.setGiven(List.of(new StringType(firstName)));
        }

        if (lastName != null && !lastName.isBlank()) {
            name.setFamily(lastName);
        }

        return name;
    }

    private List<ContactPoint> buildTelecom(String phone, String email) {
        List<ContactPoint> telecom = new ArrayList<>();

        if (phone != null && !phone.isBlank()) {
            telecom.add(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.PHONE)
                    .setValue(phone));
        }

        if (email != null && !email.isBlank()) {
            telecom.add(new ContactPoint()
                    .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                    .setValue(email));
        }

        return telecom;
    }

    private String extractFullName(RelatedPerson resource) {
        if (!resource.hasName()) {
            return null;
        }

        HumanName name = resource.getNameFirstRep();
        String firstName = null;
        String lastName = null;

        if (name.hasGiven() && !name.getGiven().isEmpty()) {
            firstName = name.getGiven().get(0).getValue();
        }

        if (name.hasFamily()) {
            lastName = name.getFamily();
        }

        if (firstName == null && lastName == null) {
            return null;
        }
        if (firstName == null) {
            return lastName;
        }
        if (lastName == null) {
            return firstName;
        }
        return firstName + " " + lastName;
    }

    private String extractRelationship(RelatedPerson resource) {
        if (!resource.hasRelationship() || resource.getRelationship().isEmpty()) {
            return null;
        }

        return support.codeableConceptToText(resource.getRelationshipFirstRep());
    }

    private String extractPhone(RelatedPerson resource) {
        if (!resource.hasTelecom() || resource.getTelecom().isEmpty()) {
            return null;
        }

        for (ContactPoint telecom : resource.getTelecom()) {
            if (telecom != null
                    && telecom.hasSystem()
                    && telecom.getSystem() == ContactPoint.ContactPointSystem.PHONE
                    && telecom.hasValue()) {
                return telecom.getValue();
            }
        }

        return null;
    }

    private String extractEmail(RelatedPerson resource) {
        if (!resource.hasTelecom() || resource.getTelecom().isEmpty()) {
            return null;
        }

        for (ContactPoint telecom : resource.getTelecom()) {
            if (telecom != null
                    && telecom.hasSystem()
                    && telecom.getSystem() == ContactPoint.ContactPointSystem.EMAIL
                    && telecom.hasValue()) {
                return telecom.getValue();
            }
        }

        return null;
    }
}