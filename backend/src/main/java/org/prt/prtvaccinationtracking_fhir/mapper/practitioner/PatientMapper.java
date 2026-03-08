package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.ContactPoint;
import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.HumanName;
import org.hl7.fhir.r5.model.Identifier;
import org.hl7.fhir.r5.model.Patient;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.CreatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.PatientDetailsDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.UpdatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.RelatedPersonDTO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PatientMapper {

    private static final String SVNR_SYSTEM = "app:svnr";

    private final MapperSupport support;

    public PatientMapper(MapperSupport support) {
        this.support = support;
    }

    public PatientDetailsDTO toDetailsDTO(Patient resource) {
        if (resource == null) {
            return null;
        }

        String firstName = extractFirstName(resource);
        String lastName = extractLastName(resource);

        return new PatientDetailsDTO(
                resource.getIdElement().getIdPart(),
                extractSvnr(resource),
                firstName,
                lastName,
                buildFullName(firstName, lastName),
                resource.hasBirthDate() ? support.toLocalDate(resource.getBirthDate()) : null,
                extractGender(resource),
                extractPhone(resource),
                extractEmail(resource),
                extractAddress(resource),
                null
        );
    }

    public Patient toResource(CreatePatientRequestDTO dto) {
        if (dto == null) {
            return null;
        }

        Patient resource = new Patient();

        if (dto.svnr() != null && !dto.svnr().isBlank()) {
            resource.setIdentifier(List.of(
                    new Identifier()
                            .setSystem(SVNR_SYSTEM)
                            .setValue(dto.svnr())
            ));
        }

        if (dto.firstName() != null || dto.lastName() != null) {
            resource.setName(List.of(buildHumanName(dto.firstName(), dto.lastName())));
        }

        if (dto.birthDate() != null) {
            resource.setBirthDate(support.toDate(dto.birthDate()));
        }

        if (dto.gender() != null) {
            resource.setGender(toAdministrativeGender(dto.gender()));
        }

        return resource;
    }

    public void updateResource(UpdatePatientRequestDTO dto, Patient resource) {
        if (dto == null || resource == null) {
            return;
        }

        if (dto.firstName() != null || dto.lastName() != null) {
            HumanName name = resource.hasName() ? resource.getNameFirstRep() : new HumanName();

            if (dto.firstName() != null) {
                if (dto.firstName().isBlank()) {
                    name.setGiven(new ArrayList<>());
                } else {
                    name.setGiven(List.of(new org.hl7.fhir.r5.model.StringType(dto.firstName())));
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

        if (dto.phone() != null || dto.email() != null) {
            List<ContactPoint> telecom = new ArrayList<>();

            if (dto.phone() != null && !dto.phone().isBlank()) {
                telecom.add(new ContactPoint()
                        .setSystem(ContactPoint.ContactPointSystem.PHONE)
                        .setValue(dto.phone()));
            }

            if (dto.email() != null && !dto.email().isBlank()) {
                telecom.add(new ContactPoint()
                        .setSystem(ContactPoint.ContactPointSystem.EMAIL)
                        .setValue(dto.email()));
            }

            resource.setTelecom(telecom);
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
            name.setGiven(List.of(new org.hl7.fhir.r5.model.StringType(firstName)));
        }

        if (lastName != null && !lastName.isBlank()) {
            name.setFamily(lastName);
        }

        return name;
    }

    private String extractSvnr(Patient resource) {
        if (!resource.hasIdentifier() || resource.getIdentifier().isEmpty()) {
            return null;
        }

        for (Identifier identifier : resource.getIdentifier()) {
            if (identifier == null || !identifier.hasValue()) {
                continue;
            }

            if (!identifier.hasSystem() || SVNR_SYSTEM.equals(identifier.getSystem())) {
                return identifier.getValue();
            }
        }

        return null;
    }

    private String extractFirstName(Patient resource) {
        if (!resource.hasName()) {
            return null;
        }

        HumanName name = resource.getNameFirstRep();
        if (!name.hasGiven() || name.getGiven().isEmpty()) {
            return null;
        }

        return name.getGiven().get(0).getValue();
    }

    private String extractLastName(Patient resource) {
        if (!resource.hasName()) {
            return null;
        }

        HumanName name = resource.getNameFirstRep();
        return name.hasFamily() ? name.getFamily() : null;
    }

    private String buildFullName(String firstName, String lastName) {
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

    private CreatePatientRequestDTO.Gender extractGender(Patient resource) {
        if (!resource.hasGender()) {
            return null;
        }

        return switch (resource.getGender()) {
            case MALE -> CreatePatientRequestDTO.Gender.male;
            case FEMALE -> CreatePatientRequestDTO.Gender.female;
            case OTHER -> CreatePatientRequestDTO.Gender.other;
            case UNKNOWN -> CreatePatientRequestDTO.Gender.unknown;
            default -> null;
        };
    }

    private Enumerations.AdministrativeGender toAdministrativeGender(CreatePatientRequestDTO.Gender gender) {
        return switch (gender) {
            case male -> Enumerations.AdministrativeGender.MALE;
            case female -> Enumerations.AdministrativeGender.FEMALE;
            case other -> Enumerations.AdministrativeGender.OTHER;
            case unknown -> Enumerations.AdministrativeGender.UNKNOWN;
        };
    }

    private String extractPhone(Patient resource) {
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

    private String extractEmail(Patient resource) {
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

    private String extractAddress(Patient resource) {
        if (!resource.hasAddress() || resource.getAddress().isEmpty()) {
            return null;
        }

        Address address = resource.getAddressFirstRep();
        return address.hasText() ? address.getText() : null;
    }

    public PatientDetailsDTO toDetailsDTO(Patient resource, RelatedPersonDTO parent) {
        PatientDetailsDTO base = toDetailsDTO(resource);
        if (base == null) {
            return null;
        }

        return new PatientDetailsDTO(
                base.id(),
                base.svnr(),
                base.firstName(),
                base.lastName(),
                base.fullName(),
                base.birthDate(),
                base.gender(),
                base.phone(),
                base.email(),
                base.address(),
                parent
        );
    }
}