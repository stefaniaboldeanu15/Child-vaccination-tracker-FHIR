package org.prt.prtvaccinationtracking_fhir.mapper.practitioner;

import org.hl7.fhir.r5.model.Enumerations;
import org.hl7.fhir.r5.model.Patient;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.CreatePatientRequestDTO;
import org.prt.prtvaccinationtracking_fhir.dto.practitioner.UpdatePatientRequestDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.hl7.fhir.r5.model.StringType;

@Component
public class PatientMapper {

    public Patient toPatient(CreatePatientRequestDTO dto) {
        Patient patient = new Patient();

        // Austrian Social Security Number (SVNR)
        patient.addIdentifier()
                .setSystem("https://elga.gv.at/svnr")
                .setValue(dto.getPatientIdentifier());

        patient.addName()
                .setFamily(dto.getLastName())
                .addGiven(dto.getFirstName());

        patient.setGender(
                Enumerations.AdministrativeGender.fromCode(dto.getGender())
        );

        patient.setBirthDate(
                java.util.Date.from(
                        LocalDate.parse(dto.getBirthDate())
                                .atStartOfDay(ZoneId.systemDefault())
                                .toInstant()
                )
        );

        return patient;
    }

    public void updatePatient(Patient patient, UpdatePatientRequestDTO dto) {

        patient.getNameFirstRep()
                .setFamily(dto.getLastName())
                .setGiven(List.of(new StringType(dto.getFirstName())));

        patient.setGender(
                Enumerations.AdministrativeGender.fromCode(dto.getGender())
        );

        // (Recommended) also update birthDate if present
        if (dto.getBirthDate() != null && !dto.getBirthDate().isBlank()) {
            patient.setBirthDate(
                    java.util.Date.from(
                            LocalDate.parse(dto.getBirthDate())
                                    .atStartOfDay(ZoneId.systemDefault())
                                    .toInstant()
                    )
            );
        }
    }
}
