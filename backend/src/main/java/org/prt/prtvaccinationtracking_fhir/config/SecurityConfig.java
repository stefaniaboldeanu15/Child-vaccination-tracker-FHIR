package org.prt.prtvaccinationtracking_fhir.config;

import org.prt.prtvaccinationtracking_fhir.auth.CredentialStore;
import org.prt.prtvaccinationtracking_fhir.auth.PractitionerCredential;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * UserDetailsService that reads users from practitioner-credentials.json
     * via CredentialStore.
     * Username == "identifier" field from JSON.
     * Password == "password" field from JSON (stored in plain text for DEV).
     */
    @Bean
    public UserDetailsService userDetailsService(CredentialStore credentialStore) {
        return username -> {
            PractitionerCredential cred = credentialStore
                    .findByIdentifier(username)
                    .orElseThrow(() ->
                            new UsernameNotFoundException("No practitioner with identifier " + username));

            // IMPORTANT: {noop} => no encoding (because passwords in JSON are plain text)
            return User.withUsername(cred.getIdentifier())
                    .password("{noop}" + cred.getPassword())
                    .roles("PRACTITIONER")
                    .build();
        };
    }

    // ===== CORS CONFIG used by Spring Security =================================
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        // Your Vite dev origin(s)
        config.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ===== SECURITY FILTER CHAIN ===============================================
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                .authorizeHttpRequests(auth -> auth
                        // Allow preflight requests
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Login endpoint is public
                        .requestMatchers("/api/auth/**").permitAll()
                        // Practitioner endpoints require PRACTITIONER role
                        .requestMatchers("/api/practitioner/**").hasRole("PRACTITIONER")
                        // Everything else is open for now
                        .anyRequest().permitAll()
                )

                // HTTP Basic for practitioner endpoints
                .httpBasic(Customizer.withDefaults())
                .formLogin(AbstractHttpConfigurer::disable);

        return http.build();
    }
}


