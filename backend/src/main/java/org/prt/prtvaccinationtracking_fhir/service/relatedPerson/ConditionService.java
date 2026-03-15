package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Condition;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.ConditionDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.CreateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.UpdateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.ConditionMapper;
import org.springframework.stereotype.Service;

@Service("relatedPersonConditionService")
public class ConditionService {

    private final FhirGateway fhir;
    private final ConditionMapper mapper;

    public ConditionService(FhirGateway fhir, ConditionMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public ConditionDTO create(CreateConditionRequestDTO dto) {
        Condition resource = mapper.toResource(dto);
        Condition created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public ConditionDTO getById(String id) {
        return mapper.toDTO(fhir.read(Condition.class, id));
    }

    public ConditionDTO update(String id, UpdateConditionRequestDTO dto) {
        Condition existing = fhir.read(Condition.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Condition.class, existing, id);
        Condition updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}