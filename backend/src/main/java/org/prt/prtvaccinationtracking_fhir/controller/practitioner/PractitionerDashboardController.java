package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.PatientClinicalOverviewDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerDashboardService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioner")
public class PractitionerDashboardController {

    private final PractitionerDashboardService dashboardService;

    public PractitionerDashboardController(PractitionerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/patients/{patientId}/overview")
    public PatientClinicalOverviewDTO getPatientOverview(@PathVariable String patientId) {
        return dashboardService.getPatientClinicalOverview(patientId);
    }
}
