package org.prt.prtvaccinationtracking_fhir.dto.practitioner;
import java.util.List;

public class CreateFullEncounterRequest {

    private String encounterId;
    private String encounterDate;
    private String organizationId;
    private String locationId;

    private List<FullImmunizationInput> immunizations;
    private List<FullObservationInput> observations;

    // ------------------------------
    // Constructor
    // ------------------------------

    public CreateFullEncounterRequest() {}

    // ------------------------------
    // Getters and Setters
    // ------------------------------

    public String getEncounterId() {
        return encounterId;
    }

    public void setEncounterId(String encounterId) {
        this.encounterId = encounterId;
    }

    public String getEncounterDate() {
        return encounterDate;
    }

    public void setEncounterDate(String encounterDate) {
        this.encounterDate = encounterDate;
    }

    public String getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(String organizationId) {
        this.organizationId = organizationId;
    }

    public String getLocationId() {
        return locationId;
    }

    public void setLocationId(String locationId) {
        this.locationId = locationId;
    }

    public List<FullImmunizationInput> getImmunizations() {
        return immunizations;
    }

    public void setImmunizations(List<FullImmunizationInput> immunizations) {
        this.immunizations = immunizations;
    }

    public List<FullObservationInput> getObservations() {
        return observations;
    }

    public void setObservations(List<FullObservationInput> observations) {
        this.observations = observations;
    }

    // ------------------------------
    // Inner classes
    // ------------------------------

    public static class FullImmunizationInput {
        private String immunizationId;
        private String vaccineCode;
        private String vaccineDisplay;
        private String occurrenceDateTime;

        public FullImmunizationInput() {}

        public String getImmunizationId() {
            return immunizationId;
        }

        public void setImmunizationId(String immunizationId) {
            this.immunizationId = immunizationId;
        }

        public String getVaccineCode() {
            return vaccineCode;
        }

        public void setVaccineCode(String vaccineCode) {
            this.vaccineCode = vaccineCode;
        }

        public String getVaccineDisplay() {
            return vaccineDisplay;
        }

        public void setVaccineDisplay(String vaccineDisplay) {
            this.vaccineDisplay = vaccineDisplay;
        }

        public String getOccurrenceDateTime() {
            return occurrenceDateTime;
        }

        public void setOccurrenceDateTime(String occurrenceDateTime) {
            this.occurrenceDateTime = occurrenceDateTime;
        }
    }


    public static class FullObservationInput {
        private String observationId;
        private String code;
        private String display;
        private String value;
        private String unit;
        private String effectiveDateTime;

        public FullObservationInput() {}

        public String getObservationId() {
            return observationId;
        }

        public void setObservationId(String observationId) {
            this.observationId = observationId;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDisplay() {
            return display;
        }

        public void setDisplay(String display) {
            this.display = display;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getEffectiveDateTime() {
            return effectiveDateTime;
        }

        public void setEffectiveDateTime(String effectiveDateTime) {
            this.effectiveDateTime = effectiveDateTime;
        }
    }
}
