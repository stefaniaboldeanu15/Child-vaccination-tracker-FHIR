package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Practitioner dashboard REST API
 *  * GET  /api/practitioner/patients/{patientId}/overview
 *  * GET  /api/practitioner/patients/{patientId}/immunizations
 *  * POST /api/practitioner/patients/{patientId}/immunizations
 *  * GET  /api/practitioner/patients/{patientId}/recommendations
 *  * POST /api/practitioner/patients/{patientId}/recommendations
 *  * GET  /api/practitioner/patients/{patientId}/appointments
 *  * POST /api/practitioner/appointments
 *  * PUT  /api/practitioner/appointments/{appointmentId}/status
 */

@RestController
@RequestMapping("/api/practitioner")
public class PractitionerPatientDashboardController {
    private final PractitionerDashboardService dashboardService;
    public PractitionerPatientDashboardController(PractitionerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    // PATIENT OVERVIEW dashboard (encounters +  allergies)

    @GetMapping("/patients/{patientId}/overview")
    public PatientClinicalOverviewDTO getOverview(@PathVariable String patientId) {
        return dashboardService.getPatientClinicalOverview(patientId);
    }
    // IMMUNIZATIONS
    @GetMapping("/patients/{patientId}/immunizations")
    public List<ImmunizationDTO> getImmunizations(@PathVariable String patientId) {
        return dashboardService.getImmunizationsForPatient(patientId);
    }
    @PostMapping("/patients/{patientId}/immunizations")
    public ImmunizationDTO createImmunization(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRequest request
    ) {
        return dashboardService.createImmunizationForPatient(patientId, request);
    }
    // RECOMMENDATIONS
    @GetMapping("/patients/{patientId}/recommendations")
    public List<ImmunizationRecommendationDTO> getRecommendations(@PathVariable String patientId) {
        return dashboardService.getRecommendationsForPatient(patientId);
    }

    @PostMapping("/patients/{patientId}/recommendations")
    public ImmunizationRecommendationDTO createRecommendation(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRecommendationRequest request
    ) {
        return dashboardService.createRecommendationForPatient(patientId, request);
    }
    // APPOINTMENTS
    @GetMapping("/patients/{patientId}/appointments")
    public List<AppointmentDTO> getAppointments(@PathVariable String patientId) {
        return dashboardService.getAppointmentsForPatient(patientId);
    }

    @PostMapping("/appointments")
    public AppointmentDTO createAppointment(@RequestBody CreateAppointmentRequest request) {
        return dashboardService.createAppointment(request);
    }

    @PutMapping("/appointments/{appointmentId}/status")
    public AppointmentDTO updateAppointmentStatus(
            @PathVariable String appointmentId,
            @RequestParam String status
    ) {
        return dashboardService.updateAppointmentStatus(appointmentId, status);
    }

}
