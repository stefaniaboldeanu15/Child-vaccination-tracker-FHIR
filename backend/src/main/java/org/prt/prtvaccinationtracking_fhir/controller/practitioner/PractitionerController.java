package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerService;
import org.springframework.web.bind.annotation.*;

@RestController("practitionerPractitionerController")
@RequestMapping("/api/practitioner/practitioners")
public class PractitionerController {

    private final PractitionerService service;

    public PractitionerController(PractitionerService service) {
        this.service = service;
    }

    @PostMapping
    public PractitionerDTO create(@RequestBody @Valid CreatePractitionerRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public PractitionerDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public PractitionerDTO update(@PathVariable String id, @RequestBody @Valid UpdatePractitionerRequestDTO dto) {
        return service.update(id, dto);
    }
}