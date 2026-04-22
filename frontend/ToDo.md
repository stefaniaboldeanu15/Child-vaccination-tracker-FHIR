- Endpoints:
    - Overview works
    - Adding and updating (email, phone and address) related person works
    - Adding and updating immunizations work
    - Adding and updating recommendations work
    - Adding a care plan work; updating works
    - Adding goal resources does work; updating works
    - Adding and updating an appointment works
    - Adding an encounter works; updating works
    - Adding an observation works; updating works
    - Adding a condition works; updating works
    - Adding and updating an allergy works
    - Adding and updating a communication does not work (problem seems to be in the backend)
    - Adding and updating adverse events works
- Real related person registration by the doctor still needed or should that even be possible? How will we solve this with the password so the practitioner does not know it? --> Token registration, check the seed bundle to see how it looks like. Password is in the extension and username is in the identifier
- Human readable errors need to be added to the frontend
- Validation of all forms for the endpoints (e.g. consents) needs to be checked and added
- Do we also want to offer the possiblility to delete a patient and/or related person? --> no delete at all
- Shouldn't the practitioner and related person not be able to update the related person name?
--> Nope, once entered, you should not be able to change the name, or birth date.
- Add mock Google and ID Austria registration/login

The Parent field exists in the patient details DTO, but practitioner patient loading does this:

PatientService.java (line 24) returns mapper.toDetailsDTO(fhir.read(Patient.class, id))
PatientMapper.java (line 40) hardcodes the last DTO field to null

The stack trace shows the crash happens in the backend mapper:

CommunicationMapper.java (line 73)
It calls payload.setContent(new StringType(dto.message()))
HAPI R5 rejects that for Communication.payload.content[x], so the server throws before the request can succeed
So even if the frontend sends valid JSON, the backend currently tries to build an invalid FHIR Communication.
