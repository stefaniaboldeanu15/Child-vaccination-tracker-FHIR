package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Practitioner;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.UpdatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonPractitionerMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonPractitionerService {

    private final FhirGateway fhir;
    private final RelatedPersonPractitionerMapper mapper;

    public RelatedPersonPractitionerService(FhirGateway fhir, RelatedPersonPractitionerMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public PractitionerDTO create(CreatePractitionerRequestDTO dto) {
        Practitioner resource = mapper.toResource(dto);
        Practitioner created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public PractitionerDTO getById(String id) {
        return mapper.toDTO(fhir.read(Practitioner.class, id));
    }

    public PractitionerDTO update(String id, UpdatePractitionerRequestDTO dto) {
        Practitioner existing = fhir.read(Practitioner.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Practitioner.class, existing, id);
        Practitioner updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}