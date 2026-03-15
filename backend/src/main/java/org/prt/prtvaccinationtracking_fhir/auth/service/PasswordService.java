package org.prt.prtvaccinationtracking_fhir.auth.service;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class PasswordService {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public boolean matches(String rawPassword, String storedValue) {
        if (rawPassword == null || storedValue == null || storedValue.isBlank()) {
            return false;
        }

        if (storedValue.startsWith("$2")) {
            return encoder.matches(rawPassword, storedValue);
        }

        return storedValue.equals(rawPassword);
    }

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }
}
