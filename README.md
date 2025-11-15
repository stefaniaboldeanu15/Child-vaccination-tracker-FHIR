# PRT - FHIR - based vaccination tracking app

---

```markdown
```
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
4. Connect the frontend (Vue.js) and backend (FastAPI + Java) with a public FHIR server.
5. Integrate vaccination schedules and automatic reminders.
6. Evaluate usability, performance, and data compliance.
7. Promote digital health literacy and patient empowerment.

---
##  System Architecture

### Example of OpenAPI FHIR Endpoint

Below is a sample from the `openapi.yaml` file showing how the `Patient` resource endpoints are defined:

```yaml
paths:
  /Patient:

  get:
    summary: Search Patient
    parameters:
      - name: _id
        in: query
        schema: { type: string }
      - name: _count
        in: query
        schema: { type: integer }
    responses:
      '200':
        description: Bundle of Patient
  post:
    summary: Create Patient
    requestBody:
      required: true
      content:
        application/fhir+json:
          schema:
            type: object
    responses:
      '201':
        description: Created
  /Patient/{id}:
```

---

### Components
| Component   | Description |
|------------ |-------------|
| Frontend    | Built with Vue.js and TailwindCSS for responsive, user-friendly interfaces. |
| Backend     | Developed in Java + FastAPI, responsible for handling API requests and connecting to the FHIR server. |
| FHIR Server | Public HAPI FHIR R4 server storing structured immunization data. |
| Database    | SQL schema used to support CDA data export and prototype persistence. |
| Version Control | Managed via Git and GitHub. |

### Data Flow
1. User interacts with the web frontend.
2. Frontend sends REST API requests to the backend.
3. Backend processes and queries the FHIR server.
4. Data is returned and displayed to the user.

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
- Node.js (v18+)
- Python 3.10+
- Java 17+
- Git

### 1. Clone the repository
```bash
git clone https://github.com/Yusrasf/PRT-main.git
cd PRT-main
````

### 2. Backend (FastAPI)

```bash
cd backend
pip install -r requirements.txt
uvicorn main:app --reload
```

### 3. Frontend (Vue.js)

```bash
cd frontend
npm install
npm run dev
```

### 4. FHIR Server Connection

The system is configured to connect to:

```
https://hapi.fhir.org/baseR4
```

You can modify this in the `.env` configuration or `apiService.js`.

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
PRT-main/
├── frontend/            # Vue.js interface
│   ├── components/
│   ├── pages/
│   ├── assets/
│   └── package.json
│
├── backend/             # FastAPI + Java backend logic
│   ├── main.py
│   ├── controllers/
│   ├── services/
│   ├── models/
│   └── requirements.txt
│
├── database/            # SQL schema and CDA templates
│   └── immunization.sql
│
├── openapi/             # OpenAPI YAML specification for FHIR endpoints
│   └── vaccination-api.yaml
│
├── README.md
└── ProjectDocumentation.pdf
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
| Name                        |                                                | Email                                                           |
| --------------------------- | --------------------------------------------- | --------------------------------------------------------------- |
| Stefania-Diana Boldeanu     |    | [me25m028@technikum-wien.at](mailto:me25m028@technikum-wien.at) |
| Lena Stadlinger             |  | [me25m031@technikum-wien.at](mailto:me25m031@technikum-wien.at) |
| Yusra Sefef                 |   | [me25m030@technikum-wien.at](mailto:me25m030@technikum-wien.at) |


---

