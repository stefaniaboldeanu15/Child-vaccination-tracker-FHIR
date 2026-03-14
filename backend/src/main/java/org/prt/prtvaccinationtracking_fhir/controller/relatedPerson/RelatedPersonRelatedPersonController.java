package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.CreateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.UpdateRelatedPersonRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonRelatedPersonService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson")
public class RelatedPersonRelatedPersonController {

    private final RelatedPersonRelatedPersonService service;

    public RelatedPersonRelatedPersonController(RelatedPersonRelatedPersonService service) {
        this.service = service;
    }

    // Create related person for a specific patient
    @PostMapping("/patients/{patientId}/related-persons")
    public RelatedPersonDTO create(@PathVariable String patientId,
                                   @RequestBody @Valid CreateRelatedPersonRequestDTO dto) {
        return service.create(patientId, dto);
    }

    @GetMapping("/related-persons/{id}")
    public RelatedPersonDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/related-persons/{id}")
    public RelatedPersonDTO update(@PathVariable String id,
                                   @RequestBody @Valid UpdateRelatedPersonRequestDTO dto) {
        return service.update(id, dto);
    }
}