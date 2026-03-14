package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Observation;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation.CreateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation.UpdateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonObservationMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonObservationService {

    private final FhirGateway fhir;
    private final RelatedPersonObservationMapper mapper;

    public RelatedPersonObservationService(FhirGateway fhir, RelatedPersonObservationMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public ObservationDTO create(CreateObservationRequestDTO dto) {
        Observation resource = mapper.toResource(dto);
        Observation created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public ObservationDTO getById(String id) {
        return mapper.toDTO(fhir.read(Observation.class, id));
    }

    public ObservationDTO update(String id, UpdateObservationRequestDTO dto) {
        Observation existing = fhir.read(Observation.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Observation.class, existing, id);
        Observation updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}