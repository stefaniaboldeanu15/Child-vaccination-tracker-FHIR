package org.prt.prtvaccinationtracking_fhir.auth.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.prt.prtvaccinationtracking_fhir.auth.config.AuthProperties;
import org.prt.prtvaccinationtracking_fhir.auth.dto.AuthSessionResponse;
import org.prt.prtvaccinationtracking_fhir.auth.dto.RegisterRelatedPersonRequest;
import org.prt.prtvaccinationtracking_fhir.auth.dto.SmartConfigurationResponse;
import org.prt.prtvaccinationtracking_fhir.auth.dto.TokenResponse;
import org.prt.prtvaccinationtracking_fhir.auth.model.AuthenticatedUser;
import org.prt.prtvaccinationtracking_fhir.auth.model.PendingAuthorizationCode;
import org.prt.prtvaccinationtracking_fhir.auth.model.UserRole;
import org.prt.prtvaccinationtracking_fhir.auth.service.AuthorizationCodeService;
import org.prt.prtvaccinationtracking_fhir.auth.service.FhirAuthService;
import org.prt.prtvaccinationtracking_fhir.auth.service.JwtService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Optional;

@RestController
public class SmartLaunchController {

    private final AuthProperties properties;
    private final FhirAuthService authService;
    private final AuthorizationCodeService authorizationCodeService;
    private final JwtService jwtService;

    public SmartLaunchController(
            AuthProperties properties,
            FhirAuthService authService,
            AuthorizationCodeService authorizationCodeService,
            JwtService jwtService
    ) {
        this.properties = properties;
        this.authService = authService;
        this.authorizationCodeService = authorizationCodeService;
        this.jwtService = jwtService;
    }

    @GetMapping("/.well-known/smart-configuration")
    public SmartConfigurationResponse smartConfiguration(HttpServletRequest request) {
        String baseUrl = baseUrl(request);

        return new SmartConfigurationResponse(
                properties.getJwt().getIssuer(),
                baseUrl + "/auth/smart/authorize",
                baseUrl + "/auth/smart/token",
                List.of("authorization_code"),
                List.of("code"),
                List.of(
                        "openid",
                        "profile",
                        "launch",
                        "launch/patient",
                        "offline_access",
                        "patient/*.read",
                        "patient/*.write",
                        "user/*.read",
                        "user/*.write"
                ),
                List.of(
                        "launch-standalone",
                        "client-public",
                        "context-standalone-patient",
                        "permission-patient",
                        "permission-user"
                ),
                List.of("S256")
        );
    }

    @GetMapping(value = "/auth/smart/authorize", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> authorizeForm(
            @RequestParam String response_type,
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam String scope,
            @RequestParam String state,
            @RequestParam String code_challenge,
            @RequestParam String code_challenge_method,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String error
    ) {
        validateAuthorizationRequest(response_type, client_id, redirect_uri, code_challenge_method);
        String safeRole = role == null || role.isBlank() ? UserRole.PRACTITIONER.externalValue() : role;
        String errorBanner = (error != null && !error.isBlank())
                ? "<div class=\"alert\">Sign in failed. Please check your credentials and try again.</div>"
                : "";

        String html = """
        <!doctype html>
        <html lang="en">
          <head>
            <meta charset="utf-8"/>
            <meta name="viewport" content="width=device-width, initial-scale=1"/>
            <title>Child Vaccination Portal - SMART Sign in</title>
            <style>
              body { margin: 0; font-family: Inter, system-ui, sans-serif; background: linear-gradient(135deg, #f4f8ff 0%%, #effcf4 100%%); color: #13213f; }
              .shell { min-height: 100vh; display: grid; place-items: center; padding: 24px; }
              .card { width: min(520px, 100%%); background: rgba(255,255,255,0.98); border: 1px solid #d7e3f2; border-radius: 22px; padding: 28px; box-shadow: 0 24px 48px rgba(19,33,63,0.08); }
              .brand { display: flex; align-items: center; gap: 12px; margin-bottom: 8px; }
              .brand-mark { width: 34px; height: 34px; border-radius: 10px; background: linear-gradient(135deg, #2f82ff, #1d4ed8); color: #fff; display: grid; place-items: center; font-size: 18px; font-weight: 800; }
              .brand-name { font-size: 13px; font-weight: 700; letter-spacing: .08em; color: #1d4ed8; text-transform: uppercase; }
              h1 { margin: 0 0 6px; font-size: 30px; }
              .subtitle { margin: 0; color: #5f6f92; line-height: 1.5; }
              .alert { margin-top: 14px; padding: 10px 12px; border-radius: 12px; border: 1px solid #fecaca; background: #fef2f2; color: #b91c1c; font-size: 14px; font-weight: 600; }
              label { display:block; margin: 16px 0 8px; font-weight: 700; color: #1f2937; font-size: 14px; }
              input { width: 100%%; box-sizing: border-box; border: 1px solid #cfd8ea; border-radius: 14px; padding: 12px 14px; font-size: 15px; }
              input:focus { outline: none; border-color: #2c82ff; box-shadow: 0 0 0 3px rgba(44,130,255,0.15); }
              .role-grid { display: grid; grid-template-columns: 1fr 1fr; gap: 10px; margin-top: 6px; }
              .role-input { position: absolute; opacity: 0; pointer-events: none; }
              .role-option { border: 1px solid #d1def0; border-radius: 12px; padding: 10px 12px; cursor: pointer; font-size: 14px; font-weight: 700; color: #1e3a8a; background: #f8fbff; text-align: center; }
              .role-input:checked + .role-option { background: #eaf2ff; border-color: #2c82ff; box-shadow: inset 0 0 0 1px #2c82ff; }
              button { margin-top: 20px; width: 100%%; border: 0; border-radius: 14px; padding: 12px 16px; font-size: 15px; font-weight: 700; background: #1d4ed8; color: white; cursor: pointer; }
              button:hover { background: #1e40af; }
              .hint-wrap { margin-top: 14px; border-radius: 14px; border: 1px solid #dbe8f6; background: #f9fcff; padding: 12px; }
              .hint-title { margin: 0 0 8px; font-size: 12px; letter-spacing: .08em; color: #1d4ed8; text-transform: uppercase; font-weight: 800; }
              .hint { margin: 0; font-size: 13px; color: #5f6f92; line-height: 1.45; }
            </style>
          </head>
          <body>
            <div class="shell">
              <form class="card" method="post" action="/auth/smart/authorize">
                <div class="brand">
                  <div class="brand-mark">+</div>
                  <div class="brand-name">Child Vaccination Portal</div>
                </div>
                <h1>Sign in to continue</h1>
                <p class="subtitle">Use your practitioner or parent credentials to continue the secure SMART launch flow.</p>
                %s

                <label for="role">Role</label>
                <div class="role-grid">
                  <label>
                    <input class="role-input" type="radio" name="role" value="practitioner" %s/>
                    <span class="role-option">Practitioner</span>
                  </label>
                  <label>
                    <input class="role-input" type="radio" name="role" value="related-person" %s/>
                    <span class="role-option">Related person</span>
                  </label>
                </div>

                <label for="username">Username</label>
                <input id="username" name="username" autocomplete="username" required/>

                <label for="password">Password</label>
                <input id="password" name="password" type="password" autocomplete="current-password" required/>

                <input type="hidden" name="response_type" value="%s"/>
                <input type="hidden" name="client_id" value="%s"/>
                <input type="hidden" name="redirect_uri" value="%s"/>
                <input type="hidden" name="scope" value="%s"/>
                <input type="hidden" name="state" value="%s"/>
                <input type="hidden" name="code_challenge" value="%s"/>
                <input type="hidden" name="code_challenge_method" value="%s"/>

                <button type="submit">Sign in securely</button>
                <div class="hint-wrap">
                  <p class="hint-title">Demo credentials</p>
                  <p class="hint">Practitioner: dr.mueller / pwMueller01<br/>Parent: anna.gruber.parent / Parent123!</p>
                </div>
              </form>
            </div>
          </body>
        </html>
        """.formatted(
                errorBanner,
                UserRole.PRACTITIONER.externalValue().equals(safeRole) ? "checked" : "",
                UserRole.RELATED_PERSON.externalValue().equals(safeRole) ? "checked" : "",
                escapeHtml(response_type),
                escapeHtml(client_id),
                escapeHtml(redirect_uri),
                escapeHtml(scope),
                escapeHtml(state),
                escapeHtml(code_challenge),
                escapeHtml(code_challenge_method)
        );


        return ResponseEntity.ok()
                .contentType(MediaType.TEXT_HTML)
                .body(html);
    }

    @PostMapping("/auth/smart/authorize")
    public ResponseEntity<Void> authorizeSubmit(
            @RequestParam String response_type,
            @RequestParam String client_id,
            @RequestParam String redirect_uri,
            @RequestParam String scope,
            @RequestParam String state,
            @RequestParam String code_challenge,
            @RequestParam String code_challenge_method,
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam(required = false) String role
    ) {
        validateAuthorizationRequest(response_type, client_id, redirect_uri, code_challenge_method);

        UserRole requestedRole = UserRole.fromExternalValue(role);
        Optional<AuthenticatedUser> authenticatedUser = requestedRole == UserRole.RELATED_PERSON
                ? authService.authenticateRelatedPerson(username, password)
                : authService.authenticatePractitioner(username, password);

        if (authenticatedUser.isEmpty()) {
            URI location = URI.create("/auth/smart/authorize?response_type=" + encode(response_type)
                    + "&client_id=" + encode(client_id)
                    + "&redirect_uri=" + encode(redirect_uri)
                    + "&scope=" + encode(scope)
                    + "&state=" + encode(state)
                    + "&code_challenge=" + encode(code_challenge)
                    + "&code_challenge_method=" + encode(code_challenge_method)
                    + "&role=" + encode(requestedRole.externalValue())
                    + "&error=invalid_credentials");
            return ResponseEntity.status(302).header(HttpHeaders.LOCATION, location.toString()).build();
        }

        PendingAuthorizationCode pendingCode = authorizationCodeService.issue(
                client_id,
                redirect_uri,
                scope,
                state,
                code_challenge,
                code_challenge_method,
                authenticatedUser.get()
        );

        URI location = URI.create(redirect_uri + "?code=" + encode(pendingCode.code()) + "&state=" + encode(state));
        return ResponseEntity.status(302).location(location).build();
    }

    @PostMapping(value = "/auth/smart/token", consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public TokenResponse token(
            @RequestParam String grant_type,
            @RequestParam String code,
            @RequestParam String redirect_uri,
            @RequestParam String client_id,
            @RequestParam String code_verifier
    ) {
        if (!"authorization_code".equals(grant_type)) {
            throw new IllegalArgumentException("Unsupported grant_type");
        }

        PendingAuthorizationCode pendingCode = authorizationCodeService.consume(code);
        if (pendingCode == null || pendingCode.isExpired(Instant.now())) {
            throw new IllegalArgumentException("Authorization code is invalid or expired");
        }

        if (!pendingCode.clientId().equals(client_id)) {
            throw new IllegalArgumentException("Invalid client_id");
        }
        if (!pendingCode.redirectUri().equals(redirect_uri)) {
            throw new IllegalArgumentException("Invalid redirect_uri");
        }
        if (!verifyPkce(code_verifier, pendingCode.codeChallenge(), pendingCode.codeChallengeMethod())) {
            throw new IllegalArgumentException("PKCE verification failed");
        }

        String accessToken = jwtService.createAccessToken(pendingCode.user(), pendingCode.scope());
        String patient = pendingCode.user().patientIds().isEmpty() ? null : pendingCode.user().patientIds().get(0);

        return new TokenResponse(
                accessToken,
                "Bearer",
                properties.getSmart().getAccessTokenTtlSeconds(),
                pendingCode.scope(),
                patient,
                pendingCode.user().fhirUser()
        );
    }

    @GetMapping("/api/auth/me")
    public AuthSessionResponse me(@org.springframework.security.core.annotation.AuthenticationPrincipal Jwt jwt) {
        return authService.sessionFromJwt(jwt);
    }

    @PostMapping("/api/auth/related-person/register")
    public AuthSessionResponse registerRelatedPerson(@RequestBody RegisterRelatedPersonRequest request) {
        authService.registerRelatedPerson(request);
        AuthenticatedUser user = authService.authenticateRelatedPerson(request.username(), request.password())
                .orElseThrow(() -> new IllegalStateException("Related person registration completed but login failed"));

        return authService.buildSessionResponse(user);
    }

    private void validateAuthorizationRequest(
            String responseType,
            String clientId,
            String redirectUri,
            String codeChallengeMethod
    ) {
        if (!"code".equals(responseType)) {
            throw new IllegalArgumentException("response_type must be code");
        }
        if (!properties.getSmart().getClientId().equals(clientId)) {
            throw new IllegalArgumentException("Unknown client_id");
        }
        if (!properties.getSmart().getRedirectUris().contains(redirectUri)) {
            throw new IllegalArgumentException("Unregistered redirect_uri");
        }
        if (!"S256".equalsIgnoreCase(codeChallengeMethod)) {
            throw new IllegalArgumentException("code_challenge_method must be S256");
        }
    }

    private boolean verifyPkce(String codeVerifier, String expectedChallenge, String codeChallengeMethod) {
        if (codeVerifier == null || expectedChallenge == null || !"S256".equalsIgnoreCase(codeChallengeMethod)) {
            return false;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(codeVerifier.getBytes(StandardCharsets.US_ASCII));
            String actualChallenge = Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
            return actualChallenge.equals(expectedChallenge);
        } catch (Exception exception) {
            return false;
        }
    }

    private String baseUrl(HttpServletRequest request) {
        StringBuilder builder = new StringBuilder();
        builder.append(request.getScheme()).append("://").append(request.getServerName());
        if ((request.getScheme().equals("http") && request.getServerPort() != 80)
                || (request.getScheme().equals("https") && request.getServerPort() != 443)) {
            builder.append(":").append(request.getServerPort());
        }
        return builder.toString();
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String escapeHtml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
