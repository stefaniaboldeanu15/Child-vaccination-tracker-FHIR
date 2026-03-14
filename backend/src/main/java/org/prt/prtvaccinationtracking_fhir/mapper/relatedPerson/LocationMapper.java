package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.Address;
import org.hl7.fhir.r5.model.Location;
import org.hl7.fhir.r5.model.Reference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface LocationMapper {

    @Mapping(target = "locationId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "organizationId", expression = "java(resource.getManagingOrganization() != null ? resource.getManagingOrganization().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "name", expression = "java(resource.getName())")
    @Mapping(target = "address", expression = "java(extractAddressText(resource))")
    @Mapping(target = "description", expression = "java(resource.getDescription())")
    LocationDTO toDTO(Location resource);

    default String extractAddressText(Location resource) {
        if (resource == null || !resource.hasAddress()) return null;
        Address a = resource.getAddress();
        return a.hasText() ? a.getText() : null;
    }
}


