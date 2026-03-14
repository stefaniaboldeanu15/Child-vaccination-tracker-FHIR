package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.Communication;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.CommunicationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.CreateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.UpdateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.CommunicationMapper;
import org.springframework.stereotype.Service;

@Service("practitionerCommunicationService")
public class CommunicationService {

    private final FhirGateway fhir;
    private final CommunicationMapper mapper;

    public CommunicationService(FhirGateway fhir, CommunicationMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public CommunicationDTO create(CreateCommunicationRequestDTO dto) {
        Communication resource = mapper.toResource(dto);
        Communication created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public CommunicationDTO getById(String id) {
        return mapper.toDTO(fhir.read(Communication.class, id));
    }

    public CommunicationDTO update(String id, UpdateCommunicationRequestDTO dto) {
        Communication existing = fhir.read(Communication.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Communication.class, existing, id);
        Communication updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}