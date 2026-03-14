package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.UpdatePractitionerRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class RelatedPersonPractitionerMapper {

    private static final String LICENSE_SYSTEM = "app:practitioner-license";
    private static final String SPECIALIZATION_SYSTEM = "app:practitioner-specialization";
    private static final String ORGANIZATION_SYSTEM = "app:organization-id";

    private final RelatedPersonMapperSupport support;

    public RelatedPersonPractitionerMapper(RelatedPersonMapperSupport support) {
        this.support = support;
    }

    public PractitionerDTO toDTO(Practitioner resource) {
        if (resource == null) {
            return null;
        }

        return new PractitionerDTO(
                resource.getIdElement().getIdPart(),
                extractFullName(resource),
                extractIdentifier(resource),
                extractRole(resource)
        );
    }

    public Practitioner toResource(CreatePractitionerRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Practitioner resource = new Practitioner();

        if (dto.firstName() != null || dto.lastName() != null) {
            resource.setName(List.of(buildHumanName(dto.firstName(), dto.lastName())));
        }

        List<Identifier> identifiers = new ArrayList<>();

        if (dto.identifier() != null && !dto.identifier().isBlank()) {
            identifiers.add(new Identifier()
                    .setSystem(LICENSE_SYSTEM)
                    .setValue(dto.identifier()));
        }

        if (dto.organizationId() != null && !dto.organizationId().isBlank()) {
            identifiers.add(new Identifier()
                    .setSystem(ORGANIZATION_SYSTEM)
                    .setValue(dto.organizationId()));
        }

        if (!identifiers.isEmpty()) {
            resource.setIdentifier(identifiers);
        }

        if (dto.specialization() != null && !dto.specialization().isBlank()) {
            resource.setQualification(List.of(
                    new Practitioner.PractitionerQualificationComponent()
                            .setCode(new CodeableConcept().addCoding(
                                    new Coding()
                                            .setSystem(SPECIALIZATION_SYSTEM)
                                            .setCode(dto.specialization())
                                            .setDisplay(dto.specialization())
                            ).setText(dto.specialization()))
            ));
        }

        List<ContactPoint> telecom = buildTelecom(dto.phone(), dto.email());
        if (!telecom.isEmpty()) {
            resource.setTelecom(telecom);
        }

        return resource;
    }

    public void updateResource(UpdatePractitionerRequestDTO dto, Practitioner resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.firstName() != null || dto.lastName() != null) {
            HumanName name = resource.hasName() ? resource.getNameFirstRep() : new HumanName();

            if (dto.firstName() != null) {
                if (dto.firstName().isBlank()) {
                    name.setGiven(new ArrayList<>());
                } else {
                    name.setGiven(List.of(new StringType(dto.firstName())));
                }
            }

            if (dto.lastName() != null) {
                name.setFamily(dto.lastName().isBlank() ? null : dto.lastName());
            }

            if (resource.hasName()) {
                resource.getName().set(0, name);
            } else {
                resource.setName(List.of(name));
            }
        }

        if (dto.specialization() != null) {
            if (dto.specialization().isBlank()) {
                resource.setQualification(new ArrayList<>());
            } else {
                resource.setQualification(List.of(
                        new Practitioner.PractitionerQualificationComponent()
                                .setCode(new CodeableConcept().addCoding(
                                        new Coding()
                                                .setSystem(SPECIALIZATION_SYSTEM)
                                                .setCode(dto.specialization())
                                                .setDisplay(dto.specialization())
                                ).setText(dto.specialization()))
                ));
            }
        }

        if (dto.phone() != null || dto.email() != null) {
            String currentPhone = extractPhone(resource);
            String currentEmail = extractEmail(resource);

            String phone = dto.phone() != null ? dto.phone() : currentPhone;
            String email = dto.email() != null ? dto.email() : currentEmail;

            resource.setTelecom(buildTelecom(phone, email));
        }

        if (dto.organizationId() != null) {
            upsertOrganizationIdentifier(resource, dto.organizationId());
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

    private String extractFullName(Practitioner resource) {
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

    private String extractIdentifier(Practitioner resource) {
        if (!resource.hasIdentifier() || resource.getIdentifier().isEmpty()) {
            return null;
        }

        for (Identifier identifier : resource.getIdentifier()) {
            if (identifier == null || !identifier.hasValue()) {
                continue;
            }

            if (!identifier.hasSystem() || LICENSE_SYSTEM.equals(identifier.getSystem())) {
                return identifier.getValue();
            }
        }

        return null;
    }

    private String extractRole(Practitioner resource) {
        if (resource.hasQualification() && !resource.getQualification().isEmpty()) {
            Practitioner.PractitionerQualificationComponent qualification = resource.getQualificationFirstRep();
            if (qualification.hasCode()) {
                return support.codeableConceptToText(qualification.getCode());
            }
        }
        return null;
    }

    private String extractPhone(Practitioner resource) {
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

    private String extractEmail(Practitioner resource) {
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

    private void upsertOrganizationIdentifier(Practitioner resource, String organizationId) {
        List<Identifier> identifiers = resource.hasIdentifier()
                ? new ArrayList<>(resource.getIdentifier())
                : new ArrayList<>();

        identifiers.removeIf(identifier ->
                identifier != null && identifier.hasSystem() && ORGANIZATION_SYSTEM.equals(identifier.getSystem()));

        if (organizationId != null && !organizationId.isBlank()) {
            identifiers.add(new Identifier()
                    .setSystem(ORGANIZATION_SYSTEM)
                    .setValue(organizationId));
        }

        resource.setIdentifier(identifiers);
    }
}