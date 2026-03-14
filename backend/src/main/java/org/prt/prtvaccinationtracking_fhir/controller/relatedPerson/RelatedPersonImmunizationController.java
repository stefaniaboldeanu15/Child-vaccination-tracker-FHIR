package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization.CreateImmunizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization.ImmunizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization.UpdateImmunizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonImmunizationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/immunizations")
public class RelatedPersonImmunizationController {

    private final RelatedPersonImmunizationService service;

    public RelatedPersonImmunizationController(RelatedPersonImmunizationService service) {
        this.service = service;
    }

    @PostMapping
    public ImmunizationDTO create(@RequestBody @Valid CreateImmunizationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public ImmunizationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ImmunizationDTO update(@PathVariable String id, @RequestBody @Valid UpdateImmunizationRequestDTO dto) {
        return service.update(id, dto);
    }
}