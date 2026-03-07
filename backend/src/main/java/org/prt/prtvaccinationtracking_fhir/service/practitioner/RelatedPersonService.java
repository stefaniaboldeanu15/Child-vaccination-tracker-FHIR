package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.RelatedPerson;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.UpdateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.RelatedPersonMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonService {

    private final FhirGateway fhir;
    private final RelatedPersonMapper mapper;

    public RelatedPersonService(FhirGateway fhir, RelatedPersonMapper mapper) {
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

        mapper.updateResourceFromDTO(dto, existing);

        fhir.ensureId(RelatedPerson.class, existing, id);

        RelatedPerson updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }
}