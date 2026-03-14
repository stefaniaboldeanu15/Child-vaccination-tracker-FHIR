package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.RelatedPerson;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.UpdateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonRelatedPersonMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonRelatedPersonService {

    private final FhirGateway fhir;
    private final RelatedPersonRelatedPersonMapper mapper;

    public RelatedPersonRelatedPersonService(FhirGateway fhir, RelatedPersonRelatedPersonMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public RelatedPersonDTO create(String patientId, CreateRelatedPersonRequestDTO dto) {
        RelatedPerson resource = mapper.toResource(dto, patientId);
        RelatedPerson created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public RelatedPersonDTO getById(String id) {
        return mapper.toDTO(fhir.read(RelatedPerson.class, id));
    }

    public RelatedPersonDTO update(String id, UpdateRelatedPersonRequestDTO dto) {
        RelatedPerson existing = fhir.read(RelatedPerson.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(RelatedPerson.class, existing, id);
        RelatedPerson updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}