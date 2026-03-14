package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Communication;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication.CommunicationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication.CreateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication.UpdateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonCommunicationMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonCommunicationService {

    private final FhirGateway fhir;
    private final RelatedPersonCommunicationMapper mapper;

    public RelatedPersonCommunicationService(FhirGateway fhir, RelatedPersonCommunicationMapper mapper) {
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