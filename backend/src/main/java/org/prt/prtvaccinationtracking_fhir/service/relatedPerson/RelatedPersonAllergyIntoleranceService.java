package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.AllergyIntolerance;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.AllergyIntoleranceDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.CreateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.allergyIntolerance.UpdateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonAllergyIntoleranceMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonAllergyIntoleranceService {

    private final FhirGateway fhir;
    private final RelatedPersonAllergyIntoleranceMapper mapper;

    public RelatedPersonAllergyIntoleranceService(FhirGateway fhir, RelatedPersonAllergyIntoleranceMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public AllergyIntoleranceDTO create(CreateAllergyIntoleranceRequestDTO dto) {
        AllergyIntolerance resource = mapper.toResource(dto);
        AllergyIntolerance created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public AllergyIntoleranceDTO getById(String id) {
        return mapper.toDTO(fhir.read(AllergyIntolerance.class, id));
    }

    public AllergyIntoleranceDTO update(String id, UpdateAllergyIntoleranceRequestDTO dto) {
        AllergyIntolerance existing = fhir.read(AllergyIntolerance.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(AllergyIntolerance.class, existing, id);
        AllergyIntolerance updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }


}