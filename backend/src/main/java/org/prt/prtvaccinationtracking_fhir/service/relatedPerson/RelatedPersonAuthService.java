package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonLoginRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonLoginResponseDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.RelatedPersonRegistrationRequestDTO;

public interface RelatedPersonAuthService {
    RelatedPersonLoginResponseDTO login(RelatedPersonLoginRequestDTO request);
    void register(RelatedPersonRegistrationRequestDTO request);
}
