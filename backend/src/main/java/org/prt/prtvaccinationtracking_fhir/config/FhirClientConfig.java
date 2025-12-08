package org.prt.prtvaccinationtracking_fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum;  // <-- NOTICE: client.api !
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FhirClientConfig {

    @Bean
    public FhirContext fhirContext() {
        // We use R5 resources
        return FhirContext.forR5();
    }

    @Bean
    public IGenericClient fhirClient(FhirContext ctx,
                                     @Value("${fhir.server.base-url}") String baseUrl) {

        // Optional: don't validate the server’s metadata each time
        ctx.getRestfulClientFactory()
                .setServerValidationMode(ServerValidationModeEnum.NEVER);

        // Plain HAPI FHIR client to your JPA server on 8080
        return ctx.newRestfulGenericClient(baseUrl);
    }
}
