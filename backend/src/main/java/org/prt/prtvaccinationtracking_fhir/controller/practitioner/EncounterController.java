package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.EncounterService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/encounters")
public class EncounterController {

    private final EncounterService service;

    public EncounterController(EncounterService service) {
        this.service = service;
    }

    @PostMapping
    public EncounterDTO create(@RequestBody @Valid CreateEncounterRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public EncounterDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public EncounterDTO update(@PathVariable String id, @RequestBody @Valid UpdateEncounterRequestDTO dto) {
        return service.update(id, dto);
    }
}