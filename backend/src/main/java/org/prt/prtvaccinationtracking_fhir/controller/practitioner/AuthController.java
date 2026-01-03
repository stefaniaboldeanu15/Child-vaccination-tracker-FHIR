package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import jakarta.annotation.PostConstruct;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.LoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.LoginResponseDTO;
//import org.prt.prtvaccinationtracking_fhir.dto.practitioner.RegistrationRequestDTO;
//import org.prt.prtvaccinationtracking_fhir.dto.practitioner.RegistrationResponseDTO;
import org.prt.prtvaccinationtracking_fhir.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * LOGIN practitioner - endpoint
 */

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostConstruct
    public void init() {
        System.out.println("AuthController LOADED");
    }

    // Home page
    @GetMapping("/")
    public String home() {
        return "Hello! API is running.";
    }

    // 1. Login endpoint - PRACTITIONER
    @PostMapping("/auth/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.authenticate(request);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

}
