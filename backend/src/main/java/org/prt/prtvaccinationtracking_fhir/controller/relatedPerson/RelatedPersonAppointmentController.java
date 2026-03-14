package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment.AppointmentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment.CreateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.appointment.UpdateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonAppointmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/relatedPerson/appointments")
public class RelatedPersonAppointmentController {

    private final RelatedPersonAppointmentService service;

    public RelatedPersonAppointmentController(RelatedPersonAppointmentService service) {
        this.service = service;
    }

    @PostMapping
    public AppointmentDTO create(@RequestBody @Valid CreateAppointmentRequestDTO dto) {
        return service.create(dto);
    }

    @GetMapping("/{id}")
    public AppointmentDTO getById(@PathVariable String id) {
        return service.getById(id);
    }

    @PutMapping("/{id}")
    public AppointmentDTO update(@PathVariable String id, @RequestBody @Valid UpdateAppointmentRequestDTO dto) {
        return service.update(id, dto);
    }
}