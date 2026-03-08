package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.Observation;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.CreateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.UpdateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.ObservationMapper;
import org.springframework.stereotype.Service;

@Service
public class ObservationService {

    private final FhirGateway fhir;
    private final ObservationMapper mapper;

    public ObservationService(FhirGateway fhir, ObservationMapper mapper) {
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