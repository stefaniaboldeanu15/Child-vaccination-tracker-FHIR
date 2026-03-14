package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.AdverseEventDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.CreateAdverseEventRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.adverseEvent.UpdateAdverseEventRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.AdverseEventService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/adverse-events")
public class AdverseEventController {

    private final AdverseEventService service;

    public AdverseEventController(AdverseEventService service) {
        this.service = service;
    }

    @PostMapping
    public AdverseEventDTO create(@RequestBody @Valid CreateAdverseEventRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public AdverseEventDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public AdverseEventDTO update(@PathVariable String id, @RequestBody @Valid UpdateAdverseEventRequestDTO dto) {
        return service.update(id, dto);
    }
}