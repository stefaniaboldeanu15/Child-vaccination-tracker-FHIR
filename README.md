
# Vaccination Tracking Made Easy: A FHIR-Based Registry for Patients and Providers

##  Project Overview
This project presents a FHIR-based Vaccination Registry designed to improve how immunization data is managed and accessed by healthcare providers and parents/legal guardians.  
The system demonstrates how HL7 FHIR standards can be applied to achieve interoperability, scalability, and data security in modern healthcare applications.

---
##  Project Goals
### Main Objective
To build a functional FHIR-based Vaccination Registry that provides secure, standardized, and user-friendly management of immunization data.

### Specific Goals
1. Design and implement a FHIR-based vaccination registry to record, store, and retrieve vaccination data.
2. Enable parents or guardians to securely access their children’s vaccination records online.
3. Ensure interoperability using HL7 FHIR resources such as:
    - `Patient`
    - `Immunization`
    - `Practitioner`
    - `Observation`
    - `Consent`
4. Connect the Vue.js-based frontend and a Java developed backend (Spring Boot) using HAPI FHIR client.
5. Integrate vaccination schedules and automatic reminders.
6. Evaluate usability, performance, and data compliance.
7. Promote digital health literacy and patient empowerment.

---
### Components
| Component   | Description                                                                                         |
|------------ |-----------------------------------------------------------------------------------------------------|
| Frontend    | Built with Vue.js, user-friendly interfaces.                                                        |
| Backend     | Java 17 Spring Boot application, build with Apache Maven, using REST APIs for server data transfers |
| FHIR Server | Public HAPI FHIR R5 server storing structured data.                                                 |
| Version Control | Managed via Git and GitHub.                                                                         |
---
### Data Flow
1. User interacts with the web frontend (practitioner/related person of the patient)
2. Frontend sends REST API requests (JSON) to the backend.
3. Backend validates the request and maps it to FHIR resources. 
4. Backend communicates with the FHIR server via HAPI FHIR. 
5. FHIR server stores or retrieves the data. 
6. FHIR server returns standardized FHIR resources. 
7. Backend maps FHIR data to DTOs and responds to the frontend.

---

##  FHIR Resources Used
| Resource      | Purpose |
|-----------    |----------|
| Patient       | Stores demographic and contact information. |
| Practitioner  | Identifies healthcare professionals administering vaccines. |
| Immunization  | Records vaccine details (type, dose, date, practitioner). |
| Consent       | Stores consent information from parents/guardians. |
| Observation   | Documents side effects or vaccination-related notes. |

---

##  Installation & Setup

### Prerequisites
- Vue.js (v18+)
- Java 17+
- Git

### 1. Clone the repository
```bash
git clone https://github.com/stefaniaboldeanu15/Child-vaccination-tracker-FHIR.git
````

### 2. Backend (FastAPI)

```bash
cd backend
mvn clean install
mvn spring-boot:run
```

### 3. Frontend (Vue.js)

```bash

```

### 4. HAPI FHIR Server Connection

The system is configured to connect to:

```
https://hapi.fhir.org/baseR5
```

---

##  Testing

* Use Postman or cURL to test endpoints (GET, POST, PUT, DELETE).
* Example:

```bash
GET https://hapi.fhir.org/baseR4/Patient
POST https://hapi.fhir.org/baseR4/Immunization
```
<img width="923" height="321" alt="image" src="https://github.com/user-attachments/assets/2b991270-348a-400a-8043-f0a9314743cc" />

Unit and integration testing performed for backend endpoints and FHIR resource transactions.

---
## Project Structure

```
Child-vaccination-tracker-FHIR/│
├── backend/src/main/java/org.prt.prtvaccinationtracking_fhir  # REST APIs + Java (SpringBoot)
│   ├── auth/ 
│   ├── config/
│   ├── controllers/ #for practitioner & related person users
│   ├── dto/
│   ├── mapper/
│   ├── service/
│   └── PrtVaccinationTrackingFhirApplication.java
│ 
├── frontend/
├── server/
└── README.md 
```

---

##  References

1. Klausen et al., “A Digital Vaccination Pass Using Fast Healthcare Interoperability Resources: A Proof of Concept,” Digital (2024).
2. Khurshid et al., “FHIRedApp: A LEAP in Health Information Technology for Promoting Patient Access,”JAMIA Open (2021).
3. Yu et al., “A Digital Certificate System That Complies with International Standards,” Standards (2023).
4. Karol & Thakare, “Strengthening Immunisation Services in India through Digital Transformation,” Preventive Medicine (2024).
6. Wilford et al., “The Digital Network of Networks: Regulatory Risk and Policy Challenges of Vaccine Passports,” Eur. J. Risk Regulation (2021).

---

##  Team Members
| Name                        |                                | Email                                                           |
| --------------------------- |--------------------------------| --------------------------------------------------------------- |
| Stefania-Diana Boldeanu     | backend - practitioner user    | [me25m028@technikum-wien.at](mailto:me25m028@technikum-wien.at) |
| Lena Stadlinger             | frontend                       | [me25m031@technikum-wien.at](mailto:me25m031@technikum-wien.at) |
| Yusra Sefef                 | backend - related person user  | [me25m030@technikum-wien.at](mailto:me25m030@technikum-wien.at) |


---

