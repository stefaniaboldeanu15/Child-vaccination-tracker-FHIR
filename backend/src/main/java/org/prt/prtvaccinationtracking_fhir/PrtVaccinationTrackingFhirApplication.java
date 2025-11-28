package org.prt.prtvaccinationtracking_fhir;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@SpringBootApplication
@ServletComponentScan
public class PrtVaccinationTrackingFhirApplication {

    public static void main(String[] args) {
        SpringApplication.run(PrtVaccinationTrackingFhirApplication.class, args);
    }
}
