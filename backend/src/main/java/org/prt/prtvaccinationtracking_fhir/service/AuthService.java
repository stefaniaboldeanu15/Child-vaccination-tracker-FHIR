package org.prt.prtvaccinationtracking_fhir.service;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.LoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.LoginResponseDTO;
//import org.prt.prtvaccinationtracking_fhir.dto.practitioner.RegistrationRequestDTO;

/**
 * Defines the contract for authentication and authorization business logic.
 */
public interface AuthService {

    /**
     * Authenticates the practitioner and generates a security token.
     * @param request Contains identifier and password.
     * @return LoginResponseDTO containing the access token and practitioner details.
     */
    LoginResponseDTO authenticate(LoginRequestDTO request);
   // void register(RegistrationRequestDTO request);
}