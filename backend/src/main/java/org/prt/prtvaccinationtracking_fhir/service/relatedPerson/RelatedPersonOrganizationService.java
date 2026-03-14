package org.prt.prtvaccinationtracking_fhir.service.relatedPerson;

import org.hl7.fhir.r5.model.Organization;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.organization.CreateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.organization.OrganizationDTO;
import org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.organization.UpdateOrganizationRequestDTO;
import org.prt.prtvaccinationtracking_fhir.fhir.FhirGateway;
import org.prt.prtvaccinationtracking_fhir.mapper.relatedPerson.RelatedPersonOrganizationMapper;
import org.springframework.stereotype.Service;

@Service
public class RelatedPersonOrganizationService {

    private final FhirGateway fhir;
    private final RelatedPersonOrganizationMapper mapper;

    public RelatedPersonOrganizationService(FhirGateway fhir, RelatedPersonOrganizationMapper mapper) {
        this.fhir = fhir;
        this.mapper = mapper;
    }

    public OrganizationDTO create(CreateOrganizationRequestDTO dto) {
        Organization resource = mapper.toResource(dto);
        Organization created = fhir.create(resource);
        return mapper.toDTO(created);
    }

    public OrganizationDTO getById(String id) {
        return mapper.toDTO(fhir.read(Organization.class, id));
    }

    public OrganizationDTO update(String id, UpdateOrganizationRequestDTO dto) {
        Organization existing = fhir.read(Organization.class, id);
        mapper.updateResource(dto, existing);
        fhir.ensureId(Organization.class, existing, id);
        Organization updated = fhir.update(existing);
        return mapper.toDTO(updated);
    }

}