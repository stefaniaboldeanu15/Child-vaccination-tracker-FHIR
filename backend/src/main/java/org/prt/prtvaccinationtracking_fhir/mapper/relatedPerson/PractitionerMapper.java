// backend/src/main/java/org/prt/prtvaccinationtracking_fhir/mapper/relatedPerson/PractitionerMapper.java
package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.CodeableConcept;
import org.hl7.fhir.r5.model.Coding;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Practitioner;
import org.hl7.fhir.r5.model.StringType;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.UpdatePractitionerRequestDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component("relatedPersonPractitionerMapper")
public class PractitionerMapper {

    private static final String LICENSE_SYSTEM = "app:practitioner-license";
    private static final String SPECIALIZATION_SYSTEM = "app:practitioner-specialization";
    private static final String ORGANIZATION_SYSTEM = "app:organization-id";

    public PractitionerDTO toDTO(Practitioner resource) {
        if (resource == null) {
            return null;
        }

        return new PractitionerDTO(
                resource.getIdElement().getIdPart(),
                extractFullName(resource),
                extractIdentifier(resource, LICENSE_SYSTEM),
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
                            .setCode(new CodeableConcept()
                                    .addCoding(new Coding()
                                            .setSystem(SPECIALIZATION_SYSTEM)
                                            .setCode(dto.specialization())
                                            .setDisplay(dto.specialization()))
                                    .setText(dto.specialization()))
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

        if (dto.organizationId() != null) {
            updateIdentifier(resource, ORGANIZATION_SYSTEM, dto.organizationId());
        }

        if (dto.specialization() != null) {
            if (dto.specialization().isBlank()) {
                resource.setQualification(new ArrayList<>());
            } else {
                resource.setQualification(List.of(
                        new Practitioner.PractitionerQualificationComponent()
                                .setCode(new CodeableConcept()
                                        .addCoding(new Coding()
                                                .setSystem(SPECIALIZATION_SYSTEM)
                                                .setCode(dto.specialization())
                                                .setDisplay(dto.specialization()))
                                        .setText(dto.specialization()))
                ));
            }
        }

        updateTelecom(resource, dto.phone(), dto.email());
    }

    private String extractFullName(Practitioner resource) {
        if (!resource.hasName()) {
            return null;
        }

        HumanName name = resource.getNameFirstRep();
        String given = name.getGivenAsSingleString();
        String family = name.getFamily();

        if ((given == null || given.isBlank()) && (family == null || family.isBlank())) {
            return null;
        }

        if (given == null || given.isBlank()) {
            return family;
        }

        if (family == null || family.isBlank()) {
            return given;
        }

        return given + " " + family;
    }

    private String extractIdentifier(Practitioner resource, String system) {
        if (resource == null || !resource.hasIdentifier()) {
            return null;
        }

        return resource.getIdentifier().stream()
                .filter(identifier -> system.equals(identifier.getSystem()))
                .map(Identifier::getValue)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }

    private String extractRole(Practitioner resource) {
        if (!resource.hasQualification() || !resource.getQualificationFirstRep().hasCode()) {
            return null;
        }

        CodeableConcept code = resource.getQualificationFirstRep().getCode();

        if (code.hasText()) {
            return code.getText();
        }

        return code.getCoding().stream()
                .filter(coding -> SPECIALIZATION_SYSTEM.equals(coding.getSystem()))
                .map(coding -> {
                    if (coding.hasDisplay() && !coding.getDisplay().isBlank()) {
                        return coding.getDisplay();
                    }
                    return coding.getCode();
                })
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
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

    private void updateTelecom(Practitioner resource, String phone, String email) {
        List<ContactPoint> telecom = new ArrayList<>();

        if (phone != null) {
            if (!phone.isBlank()) {
                telecom.add(new ContactPoint()
                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                        .setValue(phone));
            }
        } else {
            resource.getTelecom().stream()
                    .filter(cp -> cp.getSystem() == ContactPoint.ContactPointSystem.PHONE)
                    .findFirst()
                    .ifPresent(telecom::add);
        }

        if (email != null) {
            if (!email.isBlank()) {
                telecom.add(new ContactPoint()
                        .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                        .setValue(email));
            }
        } else {
            resource.getTelecom().stream()
                    .filter(cp -> cp.getSystem() == ContactPoint.ContactPointSystem.EMAIL)
                    .findFirst()
                    .ifPresent(telecom::add);
        }

        resource.setTelecom(telecom);
    }

    private void updateIdentifier(Practitioner resource, String system, String value) {
        List<Identifier> identifiers = new ArrayList<>(
                resource.hasIdentifier() ? resource.getIdentifier() : List.of()
        );

        identifiers.removeIf(identifier -> system.equals(identifier.getSystem()));

        if (value != null && !value.isBlank()) {
            identifiers.add(new Identifier()
                    .setSystem(system)
                    .setValue(value));
        }

        resource.setIdentifier(identifiers);
    }
}