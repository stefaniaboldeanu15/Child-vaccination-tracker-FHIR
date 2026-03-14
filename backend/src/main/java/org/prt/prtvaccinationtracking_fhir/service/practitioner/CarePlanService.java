package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import org.hl7.fhir.r5.model.CarePlan;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.CarePlanDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.CreateCarePlanRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.careplan.UpdateCarePlanRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.CarePlanMapper;
import org.springframework.stereotype.Service;

@Service("practitionerCarePlanService")
public class CarePlanService {

    private final FhirGateway fhir;
    private final CarePlanMapper mapper;

    public CarePlanService(FhirGateway fhir, CarePlanMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public CarePlanDTO create(CreateCarePlanRequestDTO dto) {
        CarePlan resource = mapper.toResource(dto);
        CarePlan created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public CarePlanDTO getById(String id) {
        return mapper.toDTO(fhir.read(CarePlan.class, id));
    }

    public CarePlanDTO update(String id, UpdateCarePlanRequestDTO dto) {
        CarePlan existing = fhir.read(CarePlan.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(CarePlan.class, existing, id);
        CarePlan updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}