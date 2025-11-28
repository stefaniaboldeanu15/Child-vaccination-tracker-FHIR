package org.prt.prtvaccinationtracking_fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirClientConfig {

    @Bean
    public FhirContext fhirContext() {
        return FhirContext.forR5();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {
        String serverBase = "http://localhost:8080/fhir"; // adjust if different
        return fhirContext.newRestfulGenericClient(serverBase);
    }
}
