package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.CarePlanService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/care-plans")
public class CarePlanController {

    private final CarePlanService service;

    public CarePlanController(CarePlanService service) {
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