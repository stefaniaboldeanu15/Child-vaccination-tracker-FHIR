package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Location;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.CreateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location.UpdateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonLocationMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonLocationService {

    private final FhirGateway fhir;
    private final RelatedPersonLocationMapper mapper;

    public RelatedPersonLocationService(FhirGateway fhir, RelatedPersonLocationMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public LocationDTO create(CreateLocationRequestDTO dto) {
        Location resource = mapper.toResource(dto);
        Location created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public LocationDTO getById(String id) {
        return mapper.toDTO(fhir.read(Location.class, id));
    }

    public LocationDTO update(String id, UpdateLocationRequestDTO dto) {
        Location existing = fhir.read(Location.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Location.class, existing, id);
        Location updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}