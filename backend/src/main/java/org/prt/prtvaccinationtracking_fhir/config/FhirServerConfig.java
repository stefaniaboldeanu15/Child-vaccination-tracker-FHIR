package org.prt.prtvaccinationtracking_fhir.config;

import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirServerConfig {

    @Bean
    public ServletRegistrationBean<FhirRestfulServlet> fhirServletRegistration() {
        System.out.println("DEBUG: HAPI initialize() is running!");
        ServletRegistrationBean<FhirRestfulServlet> registration =
                new ServletRegistrationBean<>(new FhirRestfulServlet(), "/fhir/*");

        return registration;
    }
}
