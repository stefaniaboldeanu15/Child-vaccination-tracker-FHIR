package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.ImmunizationRecommendation;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.CreateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.ImmunizationRecommendationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.recommendation.UpdateImmunizationRecommendationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.RecommendationMapper;
import org.springframework.stereotype.Service;

@Service
public class ImmunizationRecommendationService {

    private final FhirGateway fhir;
    private final RecommendationMapper mapper;

    public ImmunizationRecommendationService(FhirGateway fhir, RecommendationMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public ImmunizationRecommendationDTO create(CreateImmunizationRecommendationRequestDTO dto) {
        ImmunizationRecommendation resource = mapper.toResource(dto);
        ImmunizationRecommendation created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public ImmunizationRecommendationDTO getById(String id) {
        return mapper.toDTO(fhir.read(ImmunizationRecommendation.class, id));
    }

    public ImmunizationRecommendationDTO update(String id, UpdateImmunizationRecommendationRequestDTO dto) {
        ImmunizationRecommendation existing = fhir.read(ImmunizationRecommendation.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(ImmunizationRecommendation.class, existing, id);
        ImmunizationRecommendation updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}