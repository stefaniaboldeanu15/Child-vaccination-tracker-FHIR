package org.prt.prtvaccinationtracking_fhir.dto.practitioner.location;

public record LocationDTO(
        String id,
        String name,         // Department
        String description,  // Room
        String address
) {
}
