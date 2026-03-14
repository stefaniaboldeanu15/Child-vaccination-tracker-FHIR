package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.Organization;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.OrganizationDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface OrganizationMapper {

    @Mapping(target = "organizationId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "name", expression = "java(resource.getName())")
    OrganizationDTO toDTO(Organization resource);
}


