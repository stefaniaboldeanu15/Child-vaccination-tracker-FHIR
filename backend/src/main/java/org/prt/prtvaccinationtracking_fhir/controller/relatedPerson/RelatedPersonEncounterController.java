package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter.CreateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter.EncounterDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter.UpdateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonEncounterService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson")
public class RelatedPersonEncounterController {

    private final RelatedPersonEncounterService service;

    public RelatedPersonEncounterController(RelatedPersonEncounterService service) {
        this.service = service;
    }

    @PostMapping("/encounters")
    public EncounterDTO create(@RequestBody @Valid CreateEncounterRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/encounters/{id}")
    public EncounterDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/encounters/{id}")
    public EncounterDTO update(@PathVariable String id,
                               @RequestBody @Valid UpdateEncounterRequestDTO dto) {
        return service.update(id, dto);
    }

}