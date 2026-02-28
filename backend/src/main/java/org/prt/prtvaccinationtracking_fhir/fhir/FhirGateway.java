package org.prt.prtvaccinationtracking_fhir.fhir;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import ca.uhn.fhir.rest.gclient.IQuery;
import org.hl7.fhir.r5.model.Bundle;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Resource;
import org.springframework.stereotype.Component;

@Component
public class FhirGateway {

    private final IGenericClient client;

    public FhirGateway(IGenericClient client) {
        this.client = client;
    }

    @SuppressWarnings("unchecked")
    public <T extends Resource> T create(T resource) {
        MethodOutcome outcome = client.create()
                .resource(resource)
                .execute();
        return (T) outcome.getResource();
    }

    public <T extends Resource> T read(Class<T> resourceType, String id) {
        return client.read()
                .resource(resourceType)
                .withId(id)
                .execute();
    }

    @SuppressWarnings("unchecked")
    public <T extends Resource> T update(T resource) {
        MethodOutcome outcome = client.update()
                .resource(resource)
                .execute();
        return (T) outcome.getResource();
    }

    public Bundle search(IQuery<Bundle> query) {
        return query.returnBundle(Bundle.class).execute();
    }

    /** Ensures resource has an id like "Goal/123" before update if needed. */
    public <T extends Resource> void ensureId(Class<T> resourceType, T resource, String idPart) {
        resource.setId(new IdType(resourceType.getSimpleName(), idPart));
    }

    /** Escape hatch for advanced queries (try not to use directly unless needed). */
    public IGenericClient client() {
        return client;
    }
}