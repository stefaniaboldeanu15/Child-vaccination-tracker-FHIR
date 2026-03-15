package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.CreateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.OrganizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.organization.UpdateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.OrganizationService;
import org.springframework.web.bind.annotation.*;

@RestController("relatedPersonOrganizationController")
@RequestMapping("/api/related-person/organizations")
public class OrganizationController {

    private final OrganizationService service;

    public OrganizationController(OrganizationService service) {
        this.service = service;
    }

    @PostMapping
    public OrganizationDTO create(@RequestBody @Valid CreateOrganizationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public OrganizationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public OrganizationDTO update(@PathVariable String id, @RequestBody @Valid UpdateOrganizationRequestDTO dto) {
        return service.update(id, dto);
    }
}