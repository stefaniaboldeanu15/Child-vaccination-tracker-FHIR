package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import jakarta.validation.Valid;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.AppointmentDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.CreateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.appointment.UpdateAppointmentRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.AppointmentService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner/appointments")
public class AppointmentController {

    private final AppointmentService service;

    public AppointmentController(AppointmentService service) {
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