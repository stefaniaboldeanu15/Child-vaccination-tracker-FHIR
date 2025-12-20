package org.prt.prtvaccinationtracking_fhir.service;

import org.prt.prtvaccinationtracking_fhir.LoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.LoginResponseDTO;
import org.prt.prtvaccinationtracking_fhir.RegistrationRequestDTO;

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
    void register(RegistrationRequestDTO request);
}