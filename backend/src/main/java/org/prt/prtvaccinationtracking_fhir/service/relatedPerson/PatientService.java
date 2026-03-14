package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Patient;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.CreatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.PatientDetailsDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.patient.UpdatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PatientMapper;
import org.springframework.stereotype.Service;

@Service("relatedPersonPatientService")
public class PatientService {

    private final FhirGateway fhir;
    private final PatientMapper mapper;

    public PatientService(FhirGateway fhir, PatientMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public PatientDetailsDTO create(CreatePatientRequestDTO dto) {
        Patient resource = mapper.toResource(dto);
        Patient created = fhir.create(resource);
        return mapper.toDetailsDTO(created);
    }

    public PatientDetailsDTO getById(String id) {
        return mapper.toDetailsDTO(fhir.read(Patient.class, id));
    }

    public PatientDetailsDTO update(String id, UpdatePatientRequestDTO dto) {
        Patient existing = fhir.read(Patient.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Patient.class, existing, id);
        Patient updated = fhir.update(existing);
        return mapper.toDetailsDTO(updated);
    }

}