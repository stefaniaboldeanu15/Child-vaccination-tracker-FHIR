package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation.CreateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation.ObservationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.observation.UpdateObservationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonObservationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/observations")
public class RelatedPersonObservationController {

    private final RelatedPersonObservationService service;

    public RelatedPersonObservationController(RelatedPersonObservationService service) {
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