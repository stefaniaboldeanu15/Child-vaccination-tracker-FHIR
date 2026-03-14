package org.prt.prtvaccinationtracking_fhir.controller.relatedPerson;

import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonLoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonLoginResponseDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.relatedPerson.RelatedPersonRegistrationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.service.relatedPerson.RelatedPersonAuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/related-person")
public class RelatedPersonAuthController {

    private final RelatedPersonAuthService patientAuthService;

    public RelatedPersonAuthController(RelatedPersonAuthService patientAuthService) {
        this.patientAuthService = patientAuthService;
    }

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody RelatedPersonRegistrationRequestDTO request) {
        try {
            patientAuthService.register(request);
            return new ResponseEntity<>(HttpStatus.CREATED);
    
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }


    }

    @PostMapping("/auth/login")
    public ResponseEntity<RelatedPersonLoginResponseDTO> login(@RequestBody RelatedPersonLoginRequestDTO request) {
        return ResponseEntity.ok(patientAuthService.login(request));
    }
}
