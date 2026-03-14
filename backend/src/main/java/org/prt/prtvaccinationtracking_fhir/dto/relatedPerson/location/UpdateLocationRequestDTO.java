package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location;

public record UpdateLocationRequestDTO(

        String name,
        String description,
        String address,
        String phone,
        String type

) {}