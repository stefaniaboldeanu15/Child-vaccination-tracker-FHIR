package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.careplan.CarePlanDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.careplan.CreateCarePlanRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.careplan.UpdateCarePlanRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonCarePlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/care-plans")
public class RelatedPersonCarePlanController {

    private final RelatedPersonCarePlanService service;

    public RelatedPersonCarePlanController(RelatedPersonCarePlanService service) {
        this.service = service;
    }

    @PostMapping
    public CarePlanDTO create(@RequestBody @Valid CreateCarePlanRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public CarePlanDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public CarePlanDTO update(@PathVariable String id, @RequestBody @Valid UpdateCarePlanRequestDTO dto) {
        return service.update(id, dto);
    }
}