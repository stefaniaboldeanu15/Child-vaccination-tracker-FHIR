package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * What’s wired to what
 *
 * GET /api/practitioner/patients
 * → getMyPatients() → FHIR search for patients with this practitioner as generalPractitioner.
 *
 * GET /api/practitioner/patients/{id}/immunizations
 * → getImmunizationsForPatient() → FHIR search Immunization?patient={id}
 *
 * POST /api/practitioner/patients/{id}/immunizations
 * → createImmunizationForPatient() → creates Immunization on FHIR server.
 *
 * GET /api/practitioner/patients/{id}/recommendations
 * → getRecommendationsForPatient()
 *
 * POST /api/practitioner/patients/{id}/recommendations
 * → createRecommendationForPatient()
 *
 * GET /api/practitioner/patients/{id}/appointments
 * → getAppointmentsForPatient()
 *
 * POST /api/practitioner/appointments
 * → createAppointment()
 *
 * PUT /api/practitioner/appointments/{appointmentId}/status?status=booked
 * → updateAppointmentStatus()
 */
@RestController
@RequestMapping("/api/practitioner")
public class PractitionerDashboardController {

    private final PractitionerDashboardService dashboardService;

    public PractitionerDashboardController(PractitionerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    // 1) PATIENT OVERVIEW
    @GetMapping("/patients/{patientId}/overview")
    public PatientClinicalOverviewDTO getPatientOverview(@PathVariable String patientId) {
        return dashboardService.getPatientClinicalOverview(patientId);
    }
    // ─────────────────────────────────────────────────────────
    // 2) PATIENT LIST FOR CURRENT PRACTITIONER
    // ─────────────────────────────────────────────────────────
    //List all patients assigned to the logged-in practitioner
    @GetMapping("/patients")
    public List<PatientDetailsDTO> getMyPatients() {
        return dashboardService.getMyPatients();
    }
    // ─────────────────────────────────────────────────────────
    // 3) immunizations
    // ─────────────────────────────────────────────────────────
    //Get immunizations for a patient
    @GetMapping("/patients/{patientId}/immunizations")
    public List<ImmunizationDTO> getImmunizations(@PathVariable String patientId) {
        return dashboardService.getImmunizationsForPatient(patientId);
    }
    // Create a new immunization for a patient
    @PostMapping("/patients/{patientId}/immunizations")
    public ImmunizationDTO createImmunization(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRequest request
    ) {
        return dashboardService.createImmunizationForPatient(patientId, request);
    }
    // ─────────────────────────────────────────────────────────
    // 4) IMMUNIZATION RECOMMENDATIONS
    // ─────────────────────────────────────────────────────────
    // Get immunization recommendations for a patient
    @GetMapping("/patients/{patientId}/recommendations")
    public List<ImmunizationRecommendationDTO> getRecommendations(@PathVariable String patientId) {
        return dashboardService.getRecommendationsForPatient(patientId);
    }
    // Create a new immunization recommendation for a patient
    @PostMapping("/patients/{patientId}/recommendations")
    public ImmunizationRecommendationDTO createRecommendation(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRecommendationRequest request
    ) {
        return dashboardService.createRecommendationForPatient(patientId, request);
    }
    // ─────────────────────────────────────────────────────────
    // 5) APPOINTMENTS
    // ─────────────────────────────────────────────────────────
    // Get appointments for a patient
    @GetMapping("/patients/{patientId}/appointments")
    public List<AppointmentDTO> getAppointments(@PathVariable String patientId) {
        return dashboardService.getAppointmentsForPatient(patientId);
    }

    // Create appointment (practitioner side)
    @PostMapping("/appointments")
    public AppointmentDTO createAppointment(@RequestBody CreateAppointmentRequest request) {
        return dashboardService.createAppointment(request);
    }

    // Update appointment status (e.g. confirm / cancel)
    @PutMapping("/appointments/{appointmentId}/status")
    public AppointmentDTO updateAppointmentStatus(
            @PathVariable String appointmentId,
            @RequestParam String status   // e.g. "booked", "cancelled", "noshow"
    ) {
        return dashboardService.updateAppointmentStatus(appointmentId, status);
    }
}
