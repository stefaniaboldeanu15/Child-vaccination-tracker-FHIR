package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.CreateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.GoalDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.goal.UpdateGoalRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.GoalService;
import org.springframework.web.bind.annotation.*;

@RestController("relatedPersonGoalController")
@RequestMapping("/api/related-person/goals")
public class GoalController {

    private final GoalService service;

    public GoalController(GoalService service) {
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