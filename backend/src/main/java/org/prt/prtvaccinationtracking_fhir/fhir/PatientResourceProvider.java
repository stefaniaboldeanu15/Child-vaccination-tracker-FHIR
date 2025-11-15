package org.prt.prtvaccinationtracking_fhir.fhir;

import ca.uhn.fhir.rest.annotation.IdParam;
import ca.uhn.fhir.rest.annotation.Read;
import ca.uhn.fhir.rest.server.IResourceProvider;
import org.hl7.fhir.r5.model.IdType;
import org.hl7.fhir.r5.model.Patient;

public class PatientResourceProvider implements IResourceProvider {

    @Override
    public Class<Patient> getResourceType() {
        return Patient.class;
    }

    @Read()
    public Patient getPatientById(@IdParam IdType id) {
        Patient patient = new Patient();
        patient.setId(id.getIdPart());
        patient.addName().setFamily("Doe").addGiven("John");
        return patient;
    }
}
