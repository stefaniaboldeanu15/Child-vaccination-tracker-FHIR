package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.CreateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.UpdateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonLocationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/locations")
public class RelatedPersonLocationController {

    private final RelatedPersonLocationService service;

    public RelatedPersonLocationController(RelatedPersonLocationService service) {
        this.service = service;
    }

    @PostMapping
    public LocationDTO create(@RequestBody @Valid CreateLocationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public LocationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public LocationDTO update(@PathVariable String id, @RequestBody @Valid UpdateLocationRequestDTO dto) {
        return service.update(id, dto);
    }
}