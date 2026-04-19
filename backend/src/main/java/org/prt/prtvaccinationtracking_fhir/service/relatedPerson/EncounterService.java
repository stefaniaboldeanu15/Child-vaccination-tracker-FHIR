package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Encounter;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter.CreateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter.EncounterDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.encounter.UpdateEncounterRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.EncounterMapper;
import org.springframework.stereotype.Service;

@Service("relatedPersonEncounterService")
public class EncounterService {

    private final FhirGateway fhir;
    private final EncounterMapper mapper;

    public EncounterService(FhirGateway fhir, EncounterMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public EncounterDTO create(CreateEncounterRequestDTO dto) {
        Encounter resource = mapper.toResource(dto);
        Encounter created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public EncounterDTO getById(String id) {
        return mapper.toDTO(fhir.read(Encounter.class, id));
    }

    public EncounterDTO update(String id, UpdateEncounterRequestDTO dto) {
        Encounter existing = fhir.read(Encounter.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Encounter.class, existing, id);
        Encounter updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}