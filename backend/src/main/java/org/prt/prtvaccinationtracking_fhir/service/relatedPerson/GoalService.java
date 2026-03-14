package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Goal;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.CreateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.GoalDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.UpdateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.GoalMapper;
import org.springframework.stereotype.Service;

@Service("relatedPersonGoalService")
public class GoalService {

    private final FhirGateway fhir;
    private final GoalMapper mapper;

    public GoalService(FhirGateway fhir, GoalMapper mapper) {
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