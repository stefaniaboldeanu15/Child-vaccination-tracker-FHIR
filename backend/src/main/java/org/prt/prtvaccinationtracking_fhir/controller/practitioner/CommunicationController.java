package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.communication.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.CommunicationService;
import org.springframework.web.bind.annotation.*;

@RestController("practitionerCommunicationController")
@RequestMapping("/api/practitioner/communication")
public class CommunicationController {

    private final CommunicationService service;

    public CommunicationController(CommunicationService service) {
        this.service = service;
    }

    @PostMapping
    public CommunicationDTO create(@RequestBody @Valid CreateCommunicationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public CommunicationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public CommunicationDTO update(@PathVariable String id, @RequestBody @Valid UpdateCommunicationRequestDTO dto) {
        return service.update(id, dto);
    }
}