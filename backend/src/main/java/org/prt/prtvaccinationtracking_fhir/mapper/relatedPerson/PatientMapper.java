package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.PatientDetailsDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

@Mapper(config = BaseMapperConfig.class)
public interface PatientMapper {

    String SVNR_SYSTEM = "urn:oid:1.2.40.0.10.1.4.3.1";

    @Mapping(target = "patientId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientIdentifier", expression = "java(extractSvnr(resource))")
    @Mapping(target = "firstName", expression = "java(extractGiven(resource))")
    @Mapping(target = "lastName", expression = "java(extractFamily(resource))")
    @Mapping(target = "birthDate", expression = "java(resource.hasBirthDate() ? toDateString(resource.getBirthDate()) : null)")
    @Mapping(target = "gender", expression = "java(resource.getGender() != null ? resource.getGender().toCode() : null)")
    PatientDetailsDTO toDTO(Patient resource);

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

    default String toDateString(Date date) {
        if (date == null) return null;
        LocalDate local = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return local.toString();
    }
}


