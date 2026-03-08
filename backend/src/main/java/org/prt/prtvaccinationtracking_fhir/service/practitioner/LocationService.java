package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.Location;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.CreateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.LocationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.location.UpdateLocationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.LocationMapper;
import org.springframework.stereotype.Service;

@Service
public class LocationService {

    private final FhirGateway fhir;
    private final LocationMapper mapper;

    public LocationService(FhirGateway fhir, LocationMapper mapper) {
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