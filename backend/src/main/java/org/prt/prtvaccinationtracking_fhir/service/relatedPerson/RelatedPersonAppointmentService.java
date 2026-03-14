package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Appointment;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment.AppointmentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment.CreateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment.UpdateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonAppointmentMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonAppointmentService {

    private final FhirGateway fhir;
    private final RelatedPersonAppointmentMapper mapper;

    public RelatedPersonAppointmentService(FhirGateway fhir, RelatedPersonAppointmentMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public AppointmentDTO create(CreateAppointmentRequestDTO dto) {
        Appointment resource = mapper.toResource(dto);
        Appointment created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public AppointmentDTO getById(String id) {
        return mapper.toDTO(fhir.read(Appointment.class, id));
    }

    public AppointmentDTO update(String id, UpdateAppointmentRequestDTO dto) {
        Appointment existing = fhir.read(Appointment.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Appointment.class, existing, id);
        Appointment updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}