package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Consent;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent.ConsentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent.CreateConsentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.consent.UpdateConsentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonConsentMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonConsentService {

    private final FhirGateway fhir;
    private final RelatedPersonConsentMapper mapper;

    public RelatedPersonConsentService(FhirGateway fhir, RelatedPersonConsentMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public ConsentDTO create(CreateConsentRequestDTO dto) {
        Consent resource = mapper.toResource(dto);
        Consent created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public ConsentDTO getById(String id) {
        return mapper.toDTO(fhir.read(Consent.class, id));
    }

    public ConsentDTO update(String id, UpdateConsentRequestDTO dto) {
        Consent existing = fhir.read(Consent.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Consent.class, existing, id);
        Consent updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}