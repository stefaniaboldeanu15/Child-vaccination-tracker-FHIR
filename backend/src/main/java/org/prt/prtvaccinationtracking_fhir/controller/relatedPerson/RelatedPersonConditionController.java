package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.ConditionDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.CreateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.condition.UpdateConditionRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonConditionService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/conditions")
public class RelatedPersonConditionController {

    private final RelatedPersonConditionService service;

    public RelatedPersonConditionController(RelatedPersonConditionService service) {
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