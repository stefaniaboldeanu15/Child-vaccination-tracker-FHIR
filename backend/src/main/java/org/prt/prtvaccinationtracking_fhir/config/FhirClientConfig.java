package org.prt.prtvaccinationtracking_fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Spring configuration for the HAPI FHIR client.
 * This class defines beans that allow other services (e.g. PatientOverviewService)
 * to talk to the FHIR server via an {@link IGenericClient}. The client is created
 * once and injected wherever needed.
 */

@Configuration
public class FhirClientConfig {

    @Bean
    public FhirContext fhirContext() {
        // Use FHIR R5
        return FhirContext.forR5();
    }

    /**
     * Creates a reusable HAPI {@link IGenericClient} for talking to the FHIR server
     * This client is injected into services that need to read/search/write
     * FHIR resources (e.g. Patient, Practitioner, Immunization).
     * @return an {@link IGenericClient} pointing to the configured base URL
     */

    @Bean
    public IGenericClient fhirClient(FhirContext fhirContext) {

        String baseUrl = "http://localhost:8080/fhir";
        return fhirContext.newRestfulGenericClient(baseUrl);
    }
}
