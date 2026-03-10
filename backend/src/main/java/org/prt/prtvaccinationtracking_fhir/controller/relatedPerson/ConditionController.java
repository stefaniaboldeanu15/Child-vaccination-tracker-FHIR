package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.condition.ConditionDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.condition.CreateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.condition.UpdateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.ConditionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/conditions")
public class ConditionController {

    private final ConditionService service;

    public ConditionController(ConditionService service) {
        this.service = service;
    }

    @PostMapping
    public ConditionDTO create(@RequestBody @Valid CreateConditionRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public ConditionDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ConditionDTO update(@PathVariable String id, @RequestBody @Valid UpdateConditionRequestDTO dto) {
        return service.update(id, dto);
    }
}