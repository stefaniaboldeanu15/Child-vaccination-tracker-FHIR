// backend/src/main/java/org/prt/prtvaccinationtracking_fhir/service/relatedPerson/PractitionerService.java
package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Practitioner;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.practitioner.UpdatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.PractitionerMapper;
import org.springframework.stereotype.Service;

@Service("relatedPersonPractitionerService")
public class PractitionerService {

    private final FhirGateway fhir;
    private final PractitionerMapper mapper;

    public PractitionerService(FhirGateway fhir, PractitionerMapper mapper) {
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