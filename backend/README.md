
# Backend – FHIR Vaccination Tracking System

##  1.Overview

This module implements the Backend of the Child Vaccination Tracking System.
It is a stateless Spring Boot application that exposes REST APIs for healthcare practitioners/related persons (parent and legal guardians) to manage patients, encounters, immunizations, observations, adverse events, and related clinical data.

All healthcare data is stored and retrieved exclusively from a FHIR server R5. No local database is used.

---
### Users

The system supports the following user roles:
1. Practitioners – healthcare professionals administering vaccinations 
2. Related Persons – parents or legal guardians of child patients (login handled via credentials)

---

### Technology Stack
1. Java 17 
2. Spring Boot 
3. Maven 
4. HAPI FHIR Client (R5)
5. Spring Security (credential-based authentication)
6. Public HAPI FHIR Server (system of record)

---
### Backend Responsibilities
The Practitioner Backend is responsible for:
- Authenticating practitioners using stored credentials 
- Acting as a FHIR client (not a data store)
- Creating, updating, and querying FHIR resources 
- Mapping FHIR resources to frontend-friendly DTOs 
- Enforcing practitioner-based access to patient data

---

### Authentication & Credentials

- Practitioner/related person login is based on credentials (username/password)
- Credentials are matched to a Practitioner.identifier or relatedperson.identifier
- Authentication is handled via Spring Security 
- No patient data is stored locally

---
### Data Flow
- Practitioner/related person logs in via frontend 
- Frontend sends REST request to backend 
- Backend authenticates practitioner 
- Backend creates/reads FHIR resources via HAPI FHIR client 
- FHIR server returns data 
- Backend maps FHIR → DTO 
- Frontend receives structured response

---
## 2. Backend - set up
Run the application
```bash
cd backend
mvn clean install
mvn spring-boot:run
```
The backend will start on:
```bash
http://localhost:8081
```
Run the server. It will start on:
```bash
http://localhost:8080
```
Run the script from the backend directory: `upload-terminology`
- Windows → PowerShell (.ps1)
- Linux/macOS → Bash (.sh)

The script will upload:
- CodeSystems – define allowed codes 
- ValueSets – constrain which codes may be used 
- Profiles (StructureDefinitions) – enforce resource constraints
---
