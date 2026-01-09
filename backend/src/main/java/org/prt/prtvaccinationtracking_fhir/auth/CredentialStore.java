package org.prt.prtvaccinationtracking_fhir.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
/**
 * Simple in-memory "credential database" for practitioners.
 * This component loads credentials from the JSON file
 *  {@code practitioner-credentials.json} on application startup and keeps
 *  them in a {@link Map} for fast lookup by identifier (SVNR).
 * It is intentionally a lightweight replacement for a real database while
 * you are developing the application.
 **/

@Component
public class CredentialStore {

    private final Map<String, PractitionerCredential> byIdentifier = new HashMap<>();

    public CredentialStore(ObjectMapper objectMapper) {
        /// Load the JSON file from the classpath (src/main/resources)
        try (InputStream is = getClass().getResourceAsStream("/practitioner-credentials.json")) {

            if (is == null) {
                /// If the file is missing, we just log a message and leave
                /// the map empty – all login attempts will then fail.
                System.out.println("DEBUG: practitioner-credentials.json NOT FOUND");
                return;
            }

            PractitionerCredential[] all =
                    objectMapper.readValue(is, PractitionerCredential[].class);

            /// Index credentials by identifier for quick lookup
            for (PractitionerCredential c : all) {
                byIdentifier.put(c.getIdentifier(), c);
            }

            System.out.println("DEBUG: Loaded " + all.length + " practitioner credentials");
        } catch (Exception e) {
            throw new RuntimeException("Failed to load practitioner-credentials.json", e);
        }
    }

    public Optional<PractitionerCredential> findByIdentifier(String identifier) {
        return Optional.ofNullable(byIdentifier.get(identifier));
    }
}
