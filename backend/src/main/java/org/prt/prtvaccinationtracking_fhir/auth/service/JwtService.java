package org.prt.prtvaccinationtracking_fhir.auth.service;

import org.prt.prtvaccinationtracking_fhir.auth.config.AuthProperties;
import org.prt.prtvaccinationtracking_fhir.auth.model.AuthenticatedUser;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class JwtService {

    private final JwtEncoder encoder;
    private final AuthProperties properties;

    public JwtService(JwtEncoder encoder, AuthProperties properties) {
        this.encoder = encoder;
        this.properties = properties;
    }

    public String createAccessToken(AuthenticatedUser user, String scope) {
        Instant now = Instant.now();

        Map<String, Object> claims = new HashMap<>();
        claims.put("scope", scope);
        claims.put("role", user.role().externalValue());
        claims.put("username", user.username());
        claims.put("display_name", user.displayName());
        claims.put("fhirUser", user.fhirUser());
        claims.put("practitioner_id", user.practitionerId());
        claims.put("related_person_ids", user.relatedPersonIds());
        claims.put("patient_ids", user.patientIds());

        JwtClaimsSet claimsSet = JwtClaimsSet.builder()
                .issuer(properties.getJwt().getIssuer())
                .issuedAt(now)
                .expiresAt(now.plusSeconds(properties.getSmart().getAccessTokenTtlSeconds()))
                .subject(user.subject())
                .claims(existing -> existing.putAll(claims))
                .build();

        JwsHeader header = JwsHeader.with(org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256).build();
        return encoder.encode(JwtEncoderParameters.from(header, claimsSet)).getTokenValue();
    }
}
