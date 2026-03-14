package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.patient.CreatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.patient.PatientDetailsDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.patient.UpdatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonPatientService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/patients")
public class RelatedPersonPatientController {

    private final RelatedPersonPatientService service;

    public RelatedPersonPatientController(RelatedPersonPatientService service) {
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