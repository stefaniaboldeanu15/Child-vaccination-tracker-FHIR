package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Goal;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal.CreateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal.GoalDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal.UpdateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonGoalMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonGoalService {

    private final FhirGateway fhir;
    private final RelatedPersonGoalMapper mapper;

    public RelatedPersonGoalService(FhirGateway fhir, RelatedPersonGoalMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public GoalDTO create(CreateGoalRequestDTO dto) {
        Goal resource = mapper.toResource(dto);
        Goal created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public GoalDTO getById(String id) {
        return mapper.toDTO(fhir.read(Goal.class, id));
    }

    public GoalDTO update(String id, UpdateGoalRequestDTO dto) {
        Goal existing = fhir.read(Goal.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Goal.class, existing, id);
        Goal updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}