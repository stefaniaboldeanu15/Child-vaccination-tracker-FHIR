package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.recommendation.CreateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.recommendation.ImmunizationRecommendationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.recommendation.UpdateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonImmunizationRecommendationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/immunization-recommendations")
public class RelatedPersonRecommendationController {

    private final RelatedPersonImmunizationRecommendationService service;

    public RelatedPersonRecommendationController(RelatedPersonImmunizationRecommendationService service) {
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