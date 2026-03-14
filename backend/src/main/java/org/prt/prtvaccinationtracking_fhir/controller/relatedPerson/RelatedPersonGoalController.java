package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal.CreateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal.GoalDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.goal.UpdateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonGoalService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/goals")
public class RelatedPersonGoalController {

    private final RelatedPersonGoalService service;

    public RelatedPersonGoalController(RelatedPersonGoalService service) {
        this.service = service;
    }

    @PostMapping
    public GoalDTO create(@RequestBody @Valid CreateGoalRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public GoalDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public GoalDTO update(@PathVariable String id, @RequestBody @Valid UpdateGoalRequestDTO dto) {
        return service.update(id, dto);
    }
}