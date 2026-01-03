package org.prt.prtvaccinationtracking_fhir.controller.practitioner;

import org.prt.prtvaccinationtracking_fhir.dto.practitioner.CreatePractitionerRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.PractitionerDTO;
import org.prt.prtvaccinationtracking_fhir.service.practitioner.PractitionerService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/practitioners")
public class PractitionerController {

    private final PractitionerService practitionerService;

    public PractitionerController(PractitionerService practitionerService) {
        this.practitionerService = practitionerService;
    }

    @PostMapping
    public PractitionerDTO createPractitioner(
            @RequestBody CreatePractitionerRequestDTO request
    ) {
        return practitionerService.createPractitioner(request);
    }
}
