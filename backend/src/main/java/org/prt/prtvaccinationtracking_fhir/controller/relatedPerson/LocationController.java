package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.CreateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.UpdateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.LocationService;
import org.springframework.web.bind.annotation.*;

@RestController("relatedPersonLocationController")
@RequestMapping("/api/related-person/locations")
public class LocationController {

    private final LocationService service;

    public LocationController(LocationService service) {
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