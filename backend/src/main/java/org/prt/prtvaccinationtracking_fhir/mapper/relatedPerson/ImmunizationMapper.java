package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.ImmunizationDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface ImmunizationMapper {

    String CVX_SYSTEM = "http://hl7.org/fhir/sid/cvx";

    @Mapping(target = "immunizationId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getPatient() != null ? resource.getPatient().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "encounterId", expression = "java(resource.getEncounter() != null ? resource.getEncounter().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "organizationId", expression = "java(resource.getLocation() != null ? resource.getLocation().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "vaccineCode", expression = "java(extractVaccineCode(resource))")
    @Mapping(target = "vaccineDisplay", expression = "java(extractVaccineDisplay(resource))")
    @Mapping(target = "occurrenceDateTime", expression = "java(resource.hasOccurrence() ? resource.getOccurrence().primitiveValue() : null)")
    @Mapping(target = "status", expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    ImmunizationDTO toDTO(Immunization resource);

    default String extractVaccineCode(Immunization resource) {
        if (resource == null || !resource.hasVaccineCode()) return null;
        CodeableConcept cc = resource.getVaccineCode();
        for (Coding c : cc.getCoding()) {
            if (CVX_SYSTEM.equals(c.getSystem()) && c.hasCode()) return c.getCode();
        }
        return cc.hasText() ? cc.getText() : null;
    }

    default String extractVaccineDisplay(Immunization resource) {
        if (resource == null || !resource.hasVaccineCode()) return null;
        CodeableConcept cc = resource.getVaccineCode();
        for (Coding c : cc.getCoding()) {
            if (CVX_SYSTEM.equals(c.getSystem()) && c.hasDisplay()) return c.getDisplay();
        }
        return cc.hasText() ? cc.getText() : null;
    }
}


