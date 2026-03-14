package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.CreateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.ImmunizationRecommendationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.UpdateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.ImmunizationRecommendationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/immunization-recommendations")
public class RecommendationController {

    private final ImmunizationRecommendationService service;

    public RecommendationController(ImmunizationRecommendationService service) {
        this.service = service;
    }

    @PostMapping
    public ImmunizationRecommendationDTO create(@RequestBody @Valid CreateImmunizationRecommendationRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public ImmunizationRecommendationDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public ImmunizationRecommendationDTO update(@PathVariable String id,
                                                @RequestBody @Valid UpdateImmunizationRecommendationRequestDTO dto) {
        return service.update(id, dto);
    }
}