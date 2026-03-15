package org.prt.prtvaccinationtracking_fhir.auth.model;

import java.time.Instant;
import java.util.List;

public record PendingAuthorizationCode(
        String code,
        String clientId,
        String redirectUri,
        String scope,
        String state,
        String codeChallenge,
        String codeChallengeMethod,
        Instant expiresAt,
        AuthenticatedUser user
) {
    public boolean isExpired(Instant now) {
        return expiresAt == null || !expiresAt.isAfter(now);
    }
}
