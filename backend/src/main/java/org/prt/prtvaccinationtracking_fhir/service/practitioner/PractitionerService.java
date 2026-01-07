package org.prt.prtvaccinationtracking_fhir.service.practitioner;

import ca.uhn.fhir.rest.api.MethodOutcome;
import ca.uhn.fhir.rest.client.api.IGenericClient;
import org.hl7.fhir.r5.model.Practitioner;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.mapper.practitioner.PractitionerMapper;
import org.springframework.stereotype.Service;

@Service
public class PractitionerService {

    private final IGenericClient fhirClient;
    private final PractitionerMapper practitionerMapper;

    public PractitionerService(
            IGenericClient fhirClient,
            PractitionerMapper practitionerMapper
    ) {
        this.fhirClient = fhirClient;
        this.practitionerMapper = practitionerMapper;
    }

    public PractitionerDTO createPractitioner(
            CreatePractitionerRequestDTO request
    ) {
        // DTO → FHIR
        Practitioner practitioner =
                practitionerMapper.createPractitioner(request);

        // Persist
        MethodOutcome outcome = fhirClient.create()
                .resource(practitioner)
                .execute();

        // FHIR → DTO
        Practitioner created =
                (Practitioner) outcome.getResource();

        return practitionerMapper.toPractitioner(created);
    }
}
