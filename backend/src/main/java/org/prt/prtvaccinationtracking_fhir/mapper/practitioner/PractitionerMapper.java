package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner.UpdatePractitionerRequestDTO;

import java.util.Collections;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface PractitionerMapper {

    String LICENSE_SYSTEM = "urn:system:license";

    @Mapping(target = "id", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "fullName", expression = "java(extractFullName(resource))")
    @Mapping(target = "identifier", expression = "java(extractLicense(resource))")
    @Mapping(target = "firstName", expression = "java(extractGiven(resource))")
    @Mapping(target = "lastName", expression = "java(extractFamily(resource))")
    PractitionerDTO toDTO(Practitioner resource);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "identifier", expression = "java(dto.identifier() != null ? toLicenseIdentifierList(dto.identifier()) : Collections.emptyList())")
    @Mapping(target = "name", expression = "java(toHumanName(dto.firstName(), dto.lastName()))")
    @Mapping(target = "telecom", expression = "java(toTelecom(dto.phone(), dto.email()))")
    @Mapping(target = "qualification", expression = "java(dto.specialization() != null ? toQualification(dto.specialization()) : Collections.emptyList())")
    @Mapping(target = "organization", expression = "java(dto.organizationId() != null ? Collections.singletonList(new Reference(\"Organization/\" + dto.organizationId())) : Collections.emptyList())")
    Practitioner toResource(CreatePractitionerRequestDTO dto);

    @Mapping(target = "identifier", expression = "java(dto.identifier() != null ? toLicenseIdentifierList(dto.identifier()) : resource.getIdentifier())")
    @Mapping(target = "name", expression = "java((dto.firstName() != null || dto.lastName() != null) ? toHumanName(dto.firstName() != null ? dto.firstName() : extractGiven(resource), dto.lastName() != null ? dto.lastName() : extractFamily(resource)) : resource.getName())")
    @Mapping(target = "telecom", expression = "java((dto.phone() != null || dto.email() != null) ? toTelecom(dto.phone() != null ? dto.phone() : extractTelecom(resource, ContactPoint.ContactPointSystem.PHONE), dto.email() != null ? dto.email() : extractTelecom(resource, ContactPoint.ContactPointSystem.EMAIL)) : resource.getTelecom())")
    @Mapping(target = "qualification", expression = "java(dto.specialization() != null ? toQualification(dto.specialization()) : resource.getQualification())")
    @Mapping(target = "organization", expression = "java(dto.organizationId() != null ? Collections.singletonList(new Reference(\"Organization/\" + dto.organizationId())) : resource.getOrganization())")
    void updateResourceFromDTO(UpdatePractitionerRequestDTO dto, @MappingTarget Practitioner resource);

    default List<Identifier> toLicenseIdentifierList(String license) {
        Identifier id = new Identifier();
        id.setSystem(LICENSE_SYSTEM);
        id.setValue(license);
        return Collections.singletonList(id);
    }

    default List<HumanName> toHumanName(String firstName, String lastName) {
        HumanName hn = new HumanName();
        if (lastName != null) hn.setFamily(lastName);
        if (firstName != null) hn.setGiven(Collections.singletonList(new StringType(firstName)));
        return Collections.singletonList(hn);
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

    default List<Practitioner.PractitionerQualificationComponent> toQualification(String specialization) {
        Practitioner.PractitionerQualificationComponent q = new Practitioner.PractitionerQualificationComponent();
        q.setCode(new CodeableConcept().setText(specialization));
        return Collections.singletonList(q);
    }

    default String extractLicense(Practitioner resource) {
        if (resource == null || !resource.hasIdentifier()) return null;
        for (Identifier id : resource.getIdentifier()) {
            if (LICENSE_SYSTEM.equals(id.getSystem()) && id.hasValue()) return id.getValue();
        }
        return resource.getIdentifierFirstRep().hasValue() ? resource.getIdentifierFirstRep().getValue() : null;
    }

    default String extractGiven(Practitioner resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        if (!n.hasGiven() || n.getGiven().isEmpty()) return null;
        return n.getGiven().get(0).getValue();
    }

    default String extractFamily(Practitioner resource) {
        if (resource == null || !resource.hasName()) return null;
        HumanName n = resource.getNameFirstRep();
        return n.hasFamily() ? n.getFamily() : null;
    }

    default String extractFullName(Practitioner resource) {
        String g = extractGiven(resource);
        String f = extractFamily(resource);
        if (g == null && f == null) return null;
        if (g == null) return f;
        if (f == null) return g;
        return g + " " + f;
    }

    default String extractTelecom(Practitioner resource, ContactPoint.ContactPointSystem system) {
        if (resource == null || !resource.hasTelecom()) return null;
        for (ContactPoint cp : resource.getTelecom()) {
            if (cp.getSystem() == system && cp.hasValue()) return cp.getValue();
        }
        return null;
    }
}