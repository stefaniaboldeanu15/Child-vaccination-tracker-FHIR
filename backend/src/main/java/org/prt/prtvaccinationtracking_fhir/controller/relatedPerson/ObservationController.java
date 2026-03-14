package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.CreateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.observation.UpdateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.ObservationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/observations")
public class ObservationController {

    private final ObservationService service;

    public ObservationController(ObservationService service) {
        this.service = service;
    }

    @PostMapping
    public ObservationDTO create(@RequestBody @Valid CreateObservationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public ObservationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ObservationDTO update(@PathVariable String id, @RequestBody @Valid UpdateObservationRequestDTO dto) {
        return service.update(id, dto);
    }
}