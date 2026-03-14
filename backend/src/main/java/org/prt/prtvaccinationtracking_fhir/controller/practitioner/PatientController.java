package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PatientService;
import org.springframework.web.bind.annotation.*;

@RestController("practitionerPatientController")
@RequestMapping("/api/practitioner/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientService service) {
        this.service = service;
    }

    @PostMapping
    public PatientDetailsDTO create(@RequestBody @Valid CreatePatientRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public PatientDetailsDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public PatientDetailsDTO update(@PathVariable String id, @RequestBody @Valid UpdatePatientRequestDTO dto) {
        return service.update(id, dto);
    }
}