package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.*;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerDashboardService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/practitioner")
public class PractitionerPatientDashboardController {
    private final PractitionerDashboardService dashboardService;
    public PractitionerPatientDashboardController(PractitionerDashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }
    // PATIENT OVERVIEW dashboard (encounters +  allergies)
    //CREATE ENCOUNTER + IMMUNIZATION - LOCATION - ORGANIZATION
    @PostMapping("/{patientId}/create-full-encounters")
    @ResponseStatus(HttpStatus.CREATED)
    public void createFullEncounter(
            @PathVariable String patientId,
            @RequestBody CreateFullEncounterRequest request
    ) {
        dashboardService.createFullEncounter(request, patientId);
    }
    @GetMapping("/patients/{patientId}/overview")
    public PatientClinicalOverviewDTO getOverview(@PathVariable String patientId) {
        return dashboardService.getPatientClinicalOverview(patientId);
    }
    // LIST -IMMUNIZATIONS
    @GetMapping("/patients/{patientId}/get-immunizations")
    public List<ImmunizationDTO> getImmunizations(@PathVariable String patientId) {
        return dashboardService.getImmunizationsForPatient(patientId);
    }
    // CREATE IMMUNIZATION
    @PostMapping("/patients/{patientId}/create-immunization")
    public ImmunizationDTO createImmunization(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRequest request
    ) {
        return dashboardService.createImmunizationForPatient(patientId, request);
    }
    // LIST - ENCOUNTERS
    @GetMapping("patients/{patientId}/get-encounters")
    public List<EncounterBlockDTO> getEncounterBlocksForPatient(
            @PathVariable String patientId) {
        return dashboardService.getEncounterBlocksForPatient(patientId);
    }

    ///ALLERGIES
    @GetMapping("patients/{patientId}/get-allergies")
    public List<AllergyIntoleranceDTO> getAllergiesForPatient(
            @PathVariable String patientId) {
        return dashboardService.getAllergiesForPatient(patientId);
    }

    /// RECOMMENDATIONS
    @GetMapping("/patients/{patientId}/get-recommendations")
    public List<ImmunizationRecommendationDTO> getRecommendations(@PathVariable String patientId) {
        return dashboardService.getRecommendationsForPatient(patientId);
    }
    /// create - RECOMMENDATIONS
    @PostMapping("/patients/{patientId}/create-recommendation")
    public ImmunizationRecommendationDTO createRecommendation(
            @PathVariable String patientId,
            @RequestBody CreateImmunizationRecommendationRequest request
    ) {
        return dashboardService.createRecommendationForPatient(patientId, request);
    }
    /// get - APPOINTMENTS
    @GetMapping("/patients/{patientId}/get-appointments")
    public List<AppointmentDTO> getAppointments(@PathVariable String patientId) {
        return dashboardService.getAppointmentsForPatient(patientId);
    }
    /// create - APPOINTMENTS
    @PostMapping("/patients/{patientId}/create-appointments")
    public AppointmentDTO createAppointment(@RequestBody CreateAppointmentRequest request) {
        return dashboardService.createAppointment(request);
    }

    /// UPDATE AN APPOINTMENT
    @PutMapping("/appointments/{appointmentId}/status")
    public AppointmentDTO updateAppointmentStatus(
            @PathVariable String appointmentId,
            @RequestParam String status
    ) {
        return dashboardService.updateAppointmentStatus(appointmentId, status);
    }
    /// all adverse events for a patient
    @GetMapping("/patients/{patientId}/adverse-events")
    public List<AdverseEventDTO> getAdverseEventsForPatient(
            @PathVariable String patientId
    ) {
        return dashboardService
                .getAdverseEventsForPatient(patientId);
    }
    ///  Get adverse events for a specific encounter
    @GetMapping("/patients/{patientId}/encounters/{encounterId}/adverse-events")
    public List<AdverseEventDTO> getAdverseEventsForEncounter(
            @PathVariable String patientId,
            @PathVariable String encounterId
    ) {
        return dashboardService
                .getAdverseEventsForEncounter(patientId, encounterId);
    }
    /// create an adverse event for a patient
    @PostMapping("/patients/{patientId}/adverse-events")
    @ResponseStatus(HttpStatus.CREATED)
    public void createAdverseEvent(
            @PathVariable String patientId,
            @RequestBody CreateAdverseEventRequestDTO request
    ) {
        dashboardService.createAdverseEvent(patientId, request);
    }

}
