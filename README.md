
# Vaccination Tracking Made Easy: A FHIR-Based Registry for Patients and Providers

A FHIR-based child vaccination tracking platform with two user experiences:

- **Practitioner portal** for searching, reviewing, creating, and updating child clinical data
- **Related person portal** for parents/guardians to review the linked children and their vaccination-related information

The application uses:

- **Spring Boot + Maven** for the backend
- **Vue 3 + Vite + TypeScript + Vuestic UI** for the frontend
- **HAPI FHIR R5** as the FHIR persistence layer
- **SMART on FHIR standalone launch** style authentication flow for login

---

## Project Summary

This project implements a child vaccination tracking system based on **HL7 FHIR** resources.  
It supports practitioner-facing workflows and parent/guardian-facing workflows while keeping the data model interoperable and structured around FHIR resources.

The platform includes:

- child search and child profile loading
- immunization history
- care plans and goals
- appointments
- observations
- allergies and conditions
- consents
- communications
- adverse events
- practitioner and related person login flows
- local FHIR seeding for demo data

---

## Main Features

### Practitioner portal
The practitioner workspace can:

- search children by **name** and **SVNR**
- open a child profile
- view all DTO-backed resources exposed by the backend
- create and update child-related records
- manage care plans and goal resources
- review encounter-linked immunization and observation data

### Related person portal
The related person workspace can:

- sign in as a parent/guardian
- view linked children
- open one child at a time
- review the same core clinical information in a simpler workspace
- access immunizations, care plans, appointments, and the rest of the available backend-driven data

### SMART on FHIR auth
The project includes a SMART-on-FHIR style auth flow with:

- standalone launch entry
- login form
- authorization code flow components
- role-based login for practitioner and related person
- token/session handling between backend and frontend

---

## Architecture

### Components

| Component | Description |
|---|---|
| Frontend | Vue 3 + Vite + TypeScript + Vuestic UI |
| Backend | Spring Boot backend with DTOs, mappers, services, controllers |
| FHIR Server | HAPI FHIR R5 server used as the data store |
| Auth Layer | SMART-on-FHIR style login and authorization flow |
| Seed Data | Local transaction bundle used to preload demo data |

### Data flow

1. User opens the frontend
2. User signs in through the SMART login flow
3. Frontend calls backend APIs
4. Backend maps requests to FHIR resources and DTOs
5. Backend reads/writes data from/to the FHIR server
6. Backend returns mapped DTOs to the frontend
7. Frontend renders role-specific practitioner or related-person views

---

## FHIR Resources Used

The application works with these resources:

- `Patient`
- `Practitioner`
- `RelatedPerson`
- `Immunization`
- `CarePlan`
- `Goal`
- `Appointment`
- `Encounter`
- `Observation`
- `Condition`
- `AllergyIntolerance`
- `Consent`
- `Communication`
- `AdverseEvent`
- `Organization`
- `Location`

### Important project-specific meaning

- **Goal** resources are used as “vaccines left to do”
- **CarePlan** groups pending vaccination goals
- **Encounter** is used together with linked immunization, observation, location, and organization context

---

## Demo Credentials

### Practitioner
- **Username:** `dr.mueller`
- **Password:** `pwMueller01`

### Related person
- **Username:** `anna.gruber.parent`
- **Password:** `Parent123!`

These credentials depend on the seeded demo bundle being uploaded to the local FHIR server.

---

## Ports Used

The project is currently configured to run with:

- **FHIR server:** `http://localhost:8080/fhir`
- **Backend API:** `http://localhost:8081`
- **Frontend:** `http://localhost:5173`

If another project is already using port `8081`, stop that project first or move it to another port.

---

## Prerequisites

Install the following before running the project:

- **Java 17+**
- **Apache Maven wrapper support** via `mvnw.cmd`
- **Node.js 20+**
- **npm**
- **A local HAPI FHIR server running on port 8080**
- **Windows PowerShell** if you want to use `dev.ps1`

---

## Project Structure

```text
Child-vaccination-tracker-FHIR/
├── backend/
│   ├── .mvn/
│   ├── seed/
│   │   └── practitioner-view-bundle.json
│   ├── src/
│   │   └── main/
│   │       ├── java/org/prt/prtvaccinationtracking_fhir/
│   │       │   ├── auth/
│   │       │   ├── config/
│   │       │   ├── controller/
│   │       │   ├── dto/
│   │       │   ├── exception/
│   │       │   ├── fhir/
│   │       │   ├── mapper/
│   │       │   ├── service/
│   │       │   └── PrtVaccinationTrackingFhirApplication.java
│   │       └── resources/
│   ├── mvnw
│   ├── mvnw.cmd
│   └── pom.xml
├── frontend/
│   ├── .vite/
│   ├── node_modules/
│   ├── src/
│   │   ├── api/
│   │   ├── auth/
│   │   ├── components/
│   │   ├── config/
│   │   ├── utils/
│   │   ├── views/
│   │   ├── App.vue
│   │   ├── router.ts
│   │   ├── styles.css
│   │   └── main.ts
│   ├── .env.example
│   ├── env.d.ts
│   ├── index.html
│   ├── package.json
│   ├── package-lock.json
│   ├── tsconfig.json
│   └── vite.config.ts
├── dev.ps1
└── README.md
```

---

## Installation

### 1. Clone the repository

```bash
git clone https://github.com/stefaniaboldeanu15/Child-vaccination-tracker-FHIR.git
```

### 2. Open the project root

Example:

```powershell
cd "D:\MASTER\PRT - Vaccination Tracking FHIR - IntelliJ IDEA\Child-vaccination-tracker-FHIR"
```

---

## Running the Application

## Option A: Start everything manually

### 1. Start the FHIR server
Make sure your local HAPI FHIR server is already running on:

```text
http://localhost:8080/fhir
```

### 2. Start the backend

```powershell
cd ".\backend"
.\mvnw.cmd spring-boot:run
```

The backend should run on:

```text
http://localhost:8081
```

### 3. Start the frontend

Open a second terminal:

```powershell
cd ".\frontend"
npm install
npm run dev
```

The frontend should run on:

```text
http://localhost:5173
```

---

## Option B: Start using `dev.ps1`

Install dependencies, start services, and seed demo data:

```powershell
.\dev.ps1 -Install -Seed
```

### What `dev.ps1` does

- checks for backend and frontend structure
- starts backend on `8081` if it is not already running
- starts frontend on `5173` if it is not already running
- uploads the demo transaction bundle to the FHIR server if `-Seed` is used

---

## Seeding Demo Data

The demo bundle is stored here:

```text
backend\seed\practitioner-view-bundle.json
```

It contains demo resources for:

- practitioner
- related person
- patient
- care plan
- goals
- encounter
- immunization
- appointment
- observation
- organization
- location
- allergy/condition
- consent
- communication
- adverse event

## Frontend Notes

The frontend uses:

- Vue 3
- Vite
- TypeScript
- Vue Router
- Vuestic UI

### Important structure
The frontend must contain at least:

```text
frontend/
  package.json
  index.html
  vite.config.ts
  tsconfig.json
  env.d.ts
  src/
    main.ts
    App.vue
    router.ts
    styles.css
```

### Vite proxy behavior
The frontend proxies requests to:

- `/api/*` → backend on `http://localhost:8081`
- `/fhir/*` → FHIR server on `http://localhost:8080`

---

## Backend Notes

The backend includes:

- practitioner endpoints
- related person endpoints
- DTO mapping layer
- auth layer
- FHIR gateway integration

### Auth behavior
The backend auth flow:

- serves the SMART login page
- authenticates practitioner and related person users against seeded FHIR data
- uses seeded identifiers and password extensions from the demo bundle
- issues authorization flow state and tokens used by the frontend session

---

## Testing

### Backend / API
Use Swagger, Postman, or cURL to test backend endpoints.

### FHIR
Use browser, Postman, or cURL against the local FHIR server, for example:

```bash
GET http://localhost:8080/fhir/Patient
GET http://localhost:8080/fhir/Practitioner?identifier=dr.mueller
GET http://localhost:8080/fhir/RelatedPerson?identifier=anna.gruber.parent
```

### Frontend
Open:

```text
http://localhost:5173
```

Then test both:

- practitioner login
- related person login

---

## References

1. HL7 FHIR Specification
2. HAPI FHIR
3. SMART App Launch Framework
4. Vue 3 Documentation
5. Spring Boot Documentation

---

## Team Members
