package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.UpdatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonPractitionerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/practitioners")
public class RelatedPersonPractitionerController {

    private final RelatedPersonPractitionerService service;

    public RelatedPersonPractitionerController(RelatedPersonPractitionerService service) {
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