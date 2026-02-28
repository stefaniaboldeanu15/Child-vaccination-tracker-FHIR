package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.CreatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.PatientDetailsDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.UpdatePatientRequestDTO;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface PatientMapper {

    String SVNR_SYSTEM = "urn:oid:1.2.40.0.10.1.4.3.1";

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "svnr", expression = "java(extractSvnr(resource))")
    @Mapping(target = "firstName", expression = "java(extractGiven(resource))")
    @Mapping(target = "lastName", expression = "java(extractFamily(resource))")
    @Mapping(target = "fullName", expression = "java(extractFullName(resource))")
    @Mapping(target = "birthDate", expression = "java(toLocalDate(resource.getBirthDate()))")
    @Mapping(target = "gender", expression = "java(mapGender(resource.getGender()))")
    @Mapping(target = "phone", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE))")
    @Mapping(target = "email", expression = "java(extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL))")
    @Mapping(target = "address", expression = "java(extractAddressText(resource))")
    @Mapping(target = "parent", expression = "java(null)")
    PatientDetailsDTO toDTO(Patient resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifier", expression = "java(dto.svnr() != null ? toSvnrIdentifier(dto.svnr()) : Collections.emptyList())")
    @Mapping(target = "name", expression = "java(toHumanName(dto.firstName(), dto.lastName()))")
    @Mapping(target = "birthDate", expression = "java(dto.birthDate() != null ? toDate(dto.birthDate()) : null)")
    @Mapping(target = "gender", expression = "java(mapGender(dto.gender()))")
    @Mapping(target = "telecom", expression = "java(Collections.emptyList())")
    @Mapping(target = "address", expression = "java(Collections.emptyList())")
    Patient toResource(CreatePatientRequestDTO dto);

    @Mapping(target = "name", expression = "java((dto.firstName() != null || dto.lastName() != null) ? toHumanName(dto.firstName() != null ? dto.firstName() : extractGiven(resource), dto.lastName() != null ? dto.lastName() : extractFamily(resource)) : resource.getName())")
    @Mapping(target = "telecom", expression = "java(mergeTelecom(resource.getTelecom(), dto.phone(), dto.email()))")
    @Mapping(target = "address", expression = "java(dto.address() != null ? toAddressList(dto.address()) : resource.getAddress())")
    void updateResourceFromDTO(UpdatePatientRequestDTO dto, @MappingTarget Patient resource);

    default List<Identifier> toSvnrIdentifier(String svnr) {
        Identifier id = new Identifier();
        id.setSystem(SVNR_SYSTEM);
        id.setValue(svnr);
        return Collections.singletonList(id);
    }

    default List<HumanName> toHumanName(String firstName, String lastName) {
        HumanName hn = new HumanName();
        if (lastName != null) hn.setFamily(lastName);
        if (firstName != null) hn.setGiven(Collections.singletonList(new StringType(firstName)));
        return Collections.singletonList(hn);
    }

    default List<Address> toAddressList(String addressText) {
        return Collections.singletonList(new Address().setText(addressText));
    }

    default String extractSvnr(Patient resource) {
        if (resource == null || !resource.hasIdentifier()) return null;
        for (Identifier id : resource.getIdentifier()) {
            if (SVNR_SYSTEM.equals(id.getSystem()) && id.hasValue()) return id.getValue();
        }
        return null;
    }

    default String extractGiven(Patient resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        if (!n.hasGiven() || n.getGiven().isEmpty()) return null;
        return n.getGiven().get(0).getValue();
    }

    default String extractFamily(Patient resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        return n.hasFamily() ? n.getFamily() : null;
    }

    default String extractFullName(Patient resource) {
        String g = extractGiven(resource);
        String f = extractFamily(resource);
        if (g == null && f == null) return null;
        if (g == null) return f;
        if (f == null) return g;
        return g + " " + f;
    }

    default String extractTelecom(Patient resource, ContactPoint.ContactPointSystem system) {
        if (resource == null || !resource.hasTelecom()) return null;
        for (ContactPoint cp : resource.getTelecom()) {
            if (cp.getSystem() == system && cp.hasValue()) return cp.getValue();
        }
        return null;
    }

    default List<ContactPoint> mergeTelecom(List<ContactPoint> existing, String phone, String email) {
        List<ContactPoint> out = existing != null ? new java.util.ArrayList<>(existing) : new java.util.ArrayList<>();
        if (phone != null) upsertTelecom(out, ContactPoint.ContactPointSystem.PHONE, phone);
        if (email != null) upsertTelecom(out, ContactPoint.ContactPointSystem.EMAIL, email);
        return out;
    }

    default void upsertTelecom(List<ContactPoint> list, ContactPoint.ContactPointSystem system, String value) {
        for (ContactPoint cp : list) {
            if (cp.getSystem() == system) {
                cp.setValue(value);
                return;
            }
        }
        ContactPoint cp = new ContactPoint();
        cp.setSystem(system);
        cp.setValue(value);
        list.add(cp);
    }

    default String extractAddressText(Patient resource) {
        if (resource == null || !resource.hasAddress()) return null;
        Address a = resource.getAddressFirstRep();
        return a.hasText() ? a.getText() : null;
    }

    default Enumerations.AdministrativeGender mapGender(CreatePatientRequestDTO.Gender gender) {
        if (gender == null) return null;
        return switch (gender) {
            case male -> Enumerations.AdministrativeGender.MALE;
            case female -> Enumerations.AdministrativeGender.FEMALE;
            case other -> Enumerations.AdministrativeGender.OTHER;
            case unknown -> Enumerations.AdministrativeGender.UNKNOWN;
        };
    }

    default CreatePatientRequestDTO.Gender mapGender(Enumerations.AdministrativeGender gender) {
        if (gender == null) return null;
        return switch (gender) {
            case MALE -> CreatePatientRequestDTO.Gender.male;
            case FEMALE -> CreatePatientRequestDTO.Gender.female;
            case OTHER -> CreatePatientRequestDTO.Gender.other;
            case UNKNOWN -> CreatePatientRequestDTO.Gender.unknown;
            default -> CreatePatientRequestDTO.Gender.unknown;
        };
    }

    default LocalDate toLocalDate(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    default Date toDate(LocalDate date) {
        if (date == null) return null;
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }
}