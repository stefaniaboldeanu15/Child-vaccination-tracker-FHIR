package org.prt.prtvaccinationtracking_fhir.config;

import ca.uhn.fhir.context.FhirContext;
import ca.uhn.fhir.rest.api.EncodingEnum;
import ca.uhn.fhir.rest.server.RestfulServer;
import jakarta.servlet.ServletException;
import org.prt.prtvaccinationtracking_fhir.fhir.PatientResourceProvider;

public class FhirRestfulServlet extends RestfulServer {

    public FhirRestfulServlet() {
        super(FhirContext.forR5());
    }

    @Override
    protected void initialize() throws ServletException {
        System.out.println("DEBUG: HAPI initialize() is running!");

        setDefaultResponseEncoding(EncodingEnum.JSON);
        setDefaultPrettyPrint(true);

        // Register your Patient provider
        registerProvider(new PatientResourceProvider());
    }
}
