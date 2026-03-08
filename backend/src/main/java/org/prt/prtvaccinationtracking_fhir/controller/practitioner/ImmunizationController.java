package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.immunization.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.ImmunizationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/immunizations")
public class ImmunizationController {

    private final ImmunizationService service;

    public ImmunizationController(ImmunizationService service) {
        this.service = service;
    }

    @PostMapping
    public ImmunizationDTO create(@RequestBody @Valid CreateImmunizationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public ImmunizationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ImmunizationDTO update(@PathVariable String id, @RequestBody @Valid UpdateImmunizationRequestDTO dto) {
        return service.update(id, dto);
    }
}