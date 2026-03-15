package org.prt.prtvaccinationtracking_fhir.auth.service;

import org.prt.prtvaccinationtracking_fhir.auth.config.AuthProperties;
import org.prt.prtvaccinationtracking_fhir.auth.model.AuthenticatedUser;
import org.prt.prtvaccinationtracking_fhir.auth.model.PendingAuthorizationCode;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthorizationCodeService {

    private final AuthProperties properties;
    private final Map<String, PendingAuthorizationCode> codes = new ConcurrentHashMap<>();

    public AuthorizationCodeService(AuthProperties properties) {
        this.properties = properties;
    }

    public PendingAuthorizationCode issue(
            String clientId,
            String redirectUri,
            String scope,
            String state,
            String codeChallenge,
            String codeChallengeMethod,
            AuthenticatedUser user
    ) {
        cleanupExpired();
        String code = UUID.randomUUID().toString();
        PendingAuthorizationCode pendingCode = new PendingAuthorizationCode(
                code,
                clientId,
                redirectUri,
                scope,
                state,
                codeChallenge,
                codeChallengeMethod,
                Instant.now().plusSeconds(properties.getSmart().getAuthorizationCodeTtlSeconds()),
                user
        );
        codes.put(code, pendingCode);
        return pendingCode;
    }

    public PendingAuthorizationCode consume(String code) {
        cleanupExpired();
        if (code == null || code.isBlank()) {
            return null;
        }
        return codes.remove(code);
    }

    private void cleanupExpired() {
        Instant now = Instant.now();
        codes.entrySet().removeIf(entry -> entry.getValue().isExpired(now));
    }
}
