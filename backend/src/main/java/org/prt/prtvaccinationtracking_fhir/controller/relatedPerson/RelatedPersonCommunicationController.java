package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication.CommunicationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication.CreateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.communication.UpdateCommunicationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonCommunicationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/communications")
public class RelatedPersonCommunicationController {

    private final RelatedPersonCommunicationService service;

    public RelatedPersonCommunicationController(RelatedPersonCommunicationService service) {
        this.service = service;
    }

    @PostMapping
    public CommunicationDTO create(@RequestBody @Valid CreateCommunicationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public CommunicationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public CommunicationDTO update(@PathVariable String id, @RequestBody @Valid UpdateCommunicationRequestDTO dto) {
        return service.update(id, dto);
    }
}