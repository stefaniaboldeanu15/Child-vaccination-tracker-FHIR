import api from "./api";

export const getDashboardPatients = () =>
api.get("/practitioner/dashboard/patients");

export const getDashboardRelatedPersons = () =>
api.get("/practitioner/dashboard/related-persons");

export const searchDashboard = (query) =>
api.get(`/practitioner/dashboard/search?query=${query}`);

export const getPatientOverview = (patientId) =>
api.get(`/practitioner/patients/${patientId}/overview`);

export const getRecommendations = (patientId) =>
api.get(`/practitioner/patients/${patientId}/get-recommendations`);

export const getImmunizations = (patientId) =>
api.get(`/practitioner/patients/${patientId}/get-immunizations`);

export const getEncounters = (patientId) =>
api.get(`/practitioner/patients/${patientId}/get-encounters`);

export const getAppointments = (patientId) =>
api.get(`/practitioner/patients/${patientId}/get-appointments`);

export const getAllergies = (patientId) =>
api.get(`/practitioner/patients/${patientId}/get-allergies`);

export const createImmunization = (patientId, data) =>
api.post(`/practitioner/patients/${patientId}/create-immunization`, data);

export const createRecommendation = (patientId, data) =>
api.post(`/practitioner/patients/${patientId}/create-recommendation`, data);

export const createAppointment = (patientId, data) =>
api.post(`/practitioner/patients/${patientId}/create-appointments`, data);

export const updateAppointmentStatus = (appointmentId, data) =>
api.put(`/practitioner/appointments/${appointmentId}/status`, data);
