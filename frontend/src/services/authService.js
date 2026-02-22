import api from "./api";

export const loginPractitioner = (credentials) =>
api.post("/auth/login", credentials);

export const loginRelatedPerson = (credentials) =>
api.post("/related-person/auth/login", credentials);

export const registerRelatedPerson = (data) =>
api.post("/related-person/auth/register", data);
