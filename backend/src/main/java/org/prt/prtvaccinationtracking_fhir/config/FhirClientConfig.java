package org.prt.prtvaccinationtracking_fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.client.interceptor.LoggingInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirClientConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR5();
    }

    @Bean
    public IGenericClient fhirClient(
            FhirContext fhirContext,
            @Value("${fhir.server.base-url}") String baseUrl,
            @Value("${fhir.client.logging:false}") boolean enableLogging
    ) {
        IGenericClient client = fhirContext.newRestfulGenericClient(baseUrl);

        if (enableLogging) {
            client.registerInterceptor(new LoggingInterceptor(true));
        }

        return client;
    }
}