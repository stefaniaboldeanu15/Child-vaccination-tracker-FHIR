package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.AllergyIntolerance;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.AllergyIntoleranceDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.CreateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.allergyIntolerance.UpdateAllergyIntoleranceRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.AllergyIntoleranceMapper;
import org.springframework.stereotype.Service;

@Service("relatedPersonAllergyIntoleranceService")
public class AllergyIntoleranceService {

    private final FhirGateway fhir;
    private final AllergyIntoleranceMapper mapper;

    public AllergyIntoleranceService(FhirGateway fhir, AllergyIntoleranceMapper mapper) {
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