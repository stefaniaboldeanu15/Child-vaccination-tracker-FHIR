package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent.ConsentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent.CreateConsentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.consent.UpdateConsentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.ConsentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/consents")
public class ConsentController {

    private final ConsentService service;

    public ConsentController(ConsentService service) {
        this.service = service;
    }

    @PostMapping
    public ConsentDTO create(@RequestBody @Valid CreateConsentRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public ConsentDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ConsentDTO update(@PathVariable String id, @RequestBody @Valid UpdateConsentRequestDTO dto) {
        return service.update(id, dto);
    }
}