package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/practitioner/dashboard")
public class PractitionerDashboardController {

    private final PractitionerDashboardService service;

    public PractitionerDashboardController(
            PractitionerDashboardService service
    ) {
        this.service = service;
    }

    @GetMapping("/search")
    public List<PatientDashboardRowDTO> searchPatientBySvnr(
            @RequestParam String svnr
    ) {
        return service.searchBySvnr(svnr);
    }

    // DASHBOARD
    @GetMapping("/patients")
    public List<PatientDashboardRowDTO> getDashboard() {
        return service.getDashboardRows();
    }

    // Create PATIENT
    @PostMapping("/patients")
    public String createPatient(
            @RequestBody CreatePatientRequestDTO dto
    ) {
        return service.createPatient(dto);
    }
    // Update PATIENT
    @PutMapping("/patients/{patientId}")
    public void updatePatient(
            @PathVariable String patientId,
            @RequestBody UpdatePatientRequestDTO dto
    ) {
        service.updatePatient(patientId, dto);
    }

    // Create RELATED PERSON (parent / guardian)
    @PostMapping("/related-persons")
    public String createRelatedPerson(
            @RequestBody CreateRelatedPersonRequestDTO dto
    ) {
        return service.createRelatedPerson(dto);
    }

    // Update RELATED PERSON (parent / guardian)
    @PutMapping("/related-persons/{relatedPersonId}")
    public void updateRelatedPerson(
            @PathVariable String relatedPersonId,
            @RequestBody UpdateRelatedPersonRequestDTO dto
    ) {
        service.updateRelatedPerson(relatedPersonId, dto);
    }
}
