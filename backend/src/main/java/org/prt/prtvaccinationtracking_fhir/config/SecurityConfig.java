package org.prt.prtvaccinationtracking_fhir.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    /**
     * In-memory users for development.
     * IMPORTANT: "dr.smith" must match a Practitioner.identifier on your FHIR server.
     */
    @Bean
    public UserDetailsService userDetailsService() {
        var practitioner = User.withUsername("dr.smith")
                .password("{noop}password123")   // {noop} = no encoding (dev only!)
                .roles("PRACTITIONER")
                .build();

        return new InMemoryUserDetailsManager(practitioner);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // practitioner endpoints require PRACTITIONER role
                        .requestMatchers("/api/practitioner/**").hasRole("PRACTITIONER")
                        // everything else is still open (you can tighten this later)
                        .anyRequest().permitAll()
                )

                // enable basic auth for API testing
                .httpBasic(Customizer.withDefaults())

                // we don’t need HTML login forms
                .formLogin(form -> form.disable());

        return http.build();
    }
}
