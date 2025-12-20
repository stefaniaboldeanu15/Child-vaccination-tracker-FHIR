package org.prt.prtvaccinationtracking_fhir.controller;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerDashboardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Practitioner dashboard REST API
 *
 * GET  /api/practitioner/patients
 * GET  /api/practitioner/patients/{id}/overview
 * GET  /api/practitioner/patients/{id}/immunizations
 * POST /api/practitioner/patients/{id}/immunizations
 * GET  /api/practitioner/patients/{id}/recommendations
 * POST /api/practitioner/patients/{id}/recommendations
 * GET  /api/practitioner/patients/{id}/appointments
 * POST /api/practitioner/appointments
 * PUT  /api/practitioner/appointments/{appointmentId}/status
 */
@RestController
@RequestMapping("/api/practitioner")
public class PractitionerDashboardController {

    private final PractitionerDashboardService dashboardService;

    public PractitionerDashboardController(PractitionerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    // ─────────────────────────────────────────────────────────
    // 1) PATIENT OVERVIEW (encounters + immunizations + obs + allergies)
    // ─────────────────────────────────────────────────────────
    @GetMapping("/patients/{patientId}/overview")
    public PatientClinicalOverviewDTO getOverview(@PathVariable String patientId) {

        PatientDetailsDTO patient = dashboardService.getPatientDetails(patientId);

        List<EncounterBlockDTO> encounters =
                dashboardService.getEncounterBlocksForPatient(patientId);

        List<AllergyIntoleranceDTO> allergies =
                dashboardService.getAllergiesForPatient(patientId);

        return new PatientClinicalOverviewDTO(
                patient,
                encounters,
                allergies
        );
    }

    // ─────────────────────────────────────────────────────────
    // 2) PATIENT LIST FOR CURRENT PRACTITIONER
    // ─────────────────────────────────────────────────────────
    @GetMapping("/patients")
    public List<PatientDetailsDTO> getMyPatients() {
        return dashboardService.getMyPatients();
    }

    // ─────────────────────────────────────────────────────────
    // 3) IMMUNIZATIONS
    // ─────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────
    // 4) IMMUNIZATION RECOMMENDATIONS
    // ─────────────────────────────────────────────────────────
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

    // ─────────────────────────────────────────────────────────
    // 5) APPOINTMENTS
    // ─────────────────────────────────────────────────────────
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
            @RequestParam String status   // e.g. "booked", "cancelled", "noshow"
    ) {
        return dashboardService.updateAppointmentStatus(appointmentId, status);
    }
    // ─────────────────────────────────────────────────────────
    // 6) ENCOUNTER
    // ─────────────────────────────────────────────────────────

    @PostMapping("/patients/{patientId}/encounters/full")
    public String createFullEncounter(
            @PathVariable String patientId,
            @RequestBody CreateFullEncounterRequest request) {

        dashboardService.createFullEncounter(patientId, request);
        return "OK";
    }


}
