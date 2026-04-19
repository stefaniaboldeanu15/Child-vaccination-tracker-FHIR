package org.prt.prtvaccinationtracking_fhir.auth.model;

public enum UserRole {
    PRACTITIONER("practitioner"),
    RELATED_PERSON("related-person");

    private final String externalValue;

    UserRole(String externalValue) {
        this.externalValue = externalValue;
    }

    public String externalValue() {
        return externalValue;
    }

    public static UserRole fromExternalValue(String value) {
        if (value == null || value.isBlank()) {
            return PRACTITIONER;
        }

        for (UserRole role : values()) {
            if (role.externalValue.equalsIgnoreCase(value.trim())) {
                return role;
            }
        }

        throw new IllegalArgumentException("Unsupported role: " + value);
    }
}
