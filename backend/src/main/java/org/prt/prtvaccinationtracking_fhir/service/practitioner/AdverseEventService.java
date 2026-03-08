package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.AdverseEvent;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.AdverseEventDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.CreateAdverseEventRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.UpdateAdverseEventRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.AdverseEventMapper;
import org.springframework.stereotype.Service;

@Service
public class AdverseEventService {

    private final FhirGateway fhir;
    private final AdverseEventMapper mapper;

    public AdverseEventService(FhirGateway fhir, AdverseEventMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public AdverseEventDTO create(CreateAdverseEventRequestDTO dto) {
        AdverseEvent resource = mapper.toResource(dto);
        AdverseEvent created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public AdverseEventDTO getById(String id) {
        return mapper.toDTO(fhir.read(AdverseEvent.class, id));
    }

    public AdverseEventDTO update(String id, UpdateAdverseEventRequestDTO dto) {
        AdverseEvent existing = fhir.read(AdverseEvent.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(AdverseEvent.class, existing, id);
        AdverseEvent updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }


}