package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Immunization;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization.CreateImmunizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization.ImmunizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.immunization.UpdateImmunizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonImmunizationMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonImmunizationService {

    private final FhirGateway fhir;
    private final RelatedPersonImmunizationMapper mapper;

    public RelatedPersonImmunizationService(FhirGateway fhir, RelatedPersonImmunizationMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public ImmunizationDTO create(CreateImmunizationRequestDTO dto) {
        Immunization resource = mapper.toResource(dto);
        Immunization created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public ImmunizationDTO getById(String id) {
        return mapper.toDTO(fhir.read(Immunization.class, id));
    }

    public ImmunizationDTO update(String id, UpdateImmunizationRequestDTO dto) {
        Immunization existing = fhir.read(Immunization.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Immunization.class, existing, id);
        Immunization updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}