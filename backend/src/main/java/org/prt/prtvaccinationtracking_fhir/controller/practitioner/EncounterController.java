package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.CreateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.EncounterDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.encounter.UpdateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.EncounterService;
import org.springframework.web.bind.annotation.*;

@RestController("practitionerEncounterController")
@RequestMapping("/api/practitioner")
public class EncounterController {

    private final EncounterService service;

    public EncounterController(EncounterService service) {
        this.service = service;
    }

    @PostMapping("/encounters")
    public EncounterDTO create(@RequestBody @Valid CreateEncounterRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/encounters/{id}")
    public EncounterDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/encounters/{id}")
    public EncounterDTO update(@PathVariable String id,
                               @RequestBody @Valid UpdateEncounterRequestDTO dto) {
        return service.update(id, dto);
    }

}