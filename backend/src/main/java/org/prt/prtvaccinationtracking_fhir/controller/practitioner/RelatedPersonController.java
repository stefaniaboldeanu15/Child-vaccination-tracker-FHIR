package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.relatedPerson.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.RelatedPersonService;
import org.springframework.web.bind.annotation.*;

@RestController("practitionerRelatedPersonController")
@RequestMapping("/api/practitioner")
public class RelatedPersonController {

    private final RelatedPersonService service;

    public RelatedPersonController(RelatedPersonService service) {
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