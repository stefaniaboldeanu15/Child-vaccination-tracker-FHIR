package org.prt.prtvaccinationtracking_fhir.dto.relatedPerson.location;

public record CreateLocationRequestDTO(

        String name,
        String description,
        String address,
        String phone,
        String type   // department, room, clinic, etc.

) {}