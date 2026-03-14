package org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson;

import org.hl7.fhir.r5.model.DateTimeType;
import org.hl7.fhir.r5.model.Encounter;
import org.hl7.fhir.r5.model.Period;
import org.hl7.fhir.r5.model.Reference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.EncounterDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.BaseMapperConfig;

@Mapper(config = BaseMapperConfig.class)
public interface EncounterMapper {

    @Mapping(target = "encounterId", expression = "java(resource.getIdElement().getIdPart())")
    @Mapping(target = "patientId", expression = "java(resource.getSubject() != null ? resource.getSubject().getReferenceElement().getIdPart() : null)")
    @Mapping(target = "status", expression = "java(resource.getStatus() != null ? resource.getStatus().toCode() : null)")
    @Mapping(target = "startDateTime", expression = "java(resource.hasPeriod() && resource.getPeriod().hasStart() ? resource.getPeriod().getStartElement().primitiveValue() : null)")
    @Mapping(target = "endDateTime", expression = "java(resource.hasPeriod() && resource.getPeriod().hasEnd() ? resource.getPeriod().getEndElement().primitiveValue() : null)")
    EncounterDTO toDTO(Encounter resource);
}


