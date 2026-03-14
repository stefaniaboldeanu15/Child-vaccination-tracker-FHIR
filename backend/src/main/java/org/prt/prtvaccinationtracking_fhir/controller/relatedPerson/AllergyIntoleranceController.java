package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.AllergyIntoleranceDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.CreateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.UpdateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.AllergyIntoleranceService;
import org.springframework.web.bind.annotation.*;

@RestController("relatedPersonAllergyIntoleranceController")
@RequestMapping("/api/practitioner/allergy-intolerances")
public class AllergyIntoleranceController {

    private final AllergyIntoleranceService service;

    public AllergyIntoleranceController(AllergyIntoleranceService service) {
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