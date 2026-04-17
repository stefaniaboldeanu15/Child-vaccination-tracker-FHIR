package org.prt.prtvaccinationtracking_fhir.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "auth")
public class AuthProperties {

    private final Smart smart = new Smart();
    private final Jwt jwt = new Jwt();
    private final Cors cors = new Cors();

    public Smart getSmart() {
        return smart;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public Cors getCors() {
        return cors;
    }

    public static class Smart {
        private String clientId = "child-vax-ui";
        private List<String> redirectUris = List.of(
                "http://localhost:5173/auth/callback",
                "http://localhost:5174/auth/callback"
        );
        private long authorizationCodeTtlSeconds = 180;
        private long accessTokenTtlSeconds = 3600;

        public String getClientId() {
            return clientId;
        }

        public void setClientId(String clientId) {
            this.clientId = clientId;
        }

        public List<String> getRedirectUris() {
            return redirectUris;
        }

        public void setRedirectUris(List<String> redirectUris) {
            this.redirectUris = redirectUris;
        }

        public long getAuthorizationCodeTtlSeconds() {
            return authorizationCodeTtlSeconds;
        }

        public void setAuthorizationCodeTtlSeconds(long authorizationCodeTtlSeconds) {
            this.authorizationCodeTtlSeconds = authorizationCodeTtlSeconds;
        }

        public long getAccessTokenTtlSeconds() {
            return accessTokenTtlSeconds;
        }

        public void setAccessTokenTtlSeconds(long accessTokenTtlSeconds) {
            this.accessTokenTtlSeconds = accessTokenTtlSeconds;
        }
    }

    public static class Jwt {
        private String secret = "change-this-demo-secret-change-this-demo-secret";
        private String issuer = "http://localhost:8081";

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }
    }

    public static class Cors {
        private List<String> allowedOrigins = List.of(
                "http://localhost:5173",
                "http://localhost:5174"
        );

        public List<String> getAllowedOrigins() {
            return allowedOrigins;
        }

        public void setAllowedOrigins(List<String> allowedOrigins) {
            this.allowedOrigins = allowedOrigins;
        }
    }
}
