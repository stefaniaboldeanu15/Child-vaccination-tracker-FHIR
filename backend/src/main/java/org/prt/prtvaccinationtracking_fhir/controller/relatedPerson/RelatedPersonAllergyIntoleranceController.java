package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.AllergyIntoleranceDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.CreateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.UpdateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonAllergyIntoleranceService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/allergy-intolerances")
public class RelatedPersonAllergyIntoleranceController {

    private final RelatedPersonAllergyIntoleranceService service;

    public RelatedPersonAllergyIntoleranceController(RelatedPersonAllergyIntoleranceService service) {
        this.service = service;
    }

    @PostMapping
    public AllergyIntoleranceDTO create(@RequestBody @Valid CreateAllergyIntoleranceRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public AllergyIntoleranceDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public AllergyIntoleranceDTO update(@PathVariable String id, @RequestBody @Valid UpdateAllergyIntoleranceRequestDTO dto) {
        return service.update(id, dto);
    }
}