export type Option = { label: string; value: string }

export type FieldType = 'text' | 'textarea' | 'date' | 'datetime' | 'number' | 'select'

export type FieldConfig = {
  key: string
  label: string
  type: FieldType
  placeholder?: string
  required?: boolean
  options?: Option[]
  full?: boolean
}

export type ResourceConfig = {
  key: string
  label: string
  singularLabel?: string
  description: string
  searchPath: (patientId: string) => string | string[]
  getPath: (id: string) => string
  createPath?: string | ((patientId: string) => string)
  updatePath?: (id: string) => string
  createFields?: FieldConfig[]
  updateFields?: FieldConfig[]
  canCreate?: boolean
  canEdit?: boolean
}

const genderOptions: Option[] = [
  { label: 'male', value: 'male' },
  { label: 'female', value: 'female' },
  { label: 'other', value: 'other' },
  { label: 'unknown', value: 'unknown' },
]

const immunizationStatusOptions: Option[] = [
  { label: 'completed', value: 'completed' },
  { label: 'entered-in-error', value: 'entered_in_error' },
  { label: 'not-done', value: 'not_done' },
]

const recommendationStatusOptions: Option[] = [
  { label: 'due', value: 'due' },
  { label: 'completed', value: 'completed' },
  { label: 'overdue', value: 'overdue' },
]

const goalStatusOptions: Option[] = [
  { label: 'planned', value: 'planned' },
  { label: 'active', value: 'active' },
  { label: 'on-hold', value: 'on-hold' },
  { label: 'completed', value: 'completed' },
  { label: 'cancelled', value: 'cancelled' },
]

const allergyClinicalStatusOptions: Option[] = [
  { label: 'ACTIVE', value: 'ACTIVE' },
  { label: 'INACTIVE', value: 'INACTIVE' },
  { label: 'RESOLVED', value: 'RESOLVED' },
]

const allergyTypeOptions: Option[] = [
  { label: 'ALLERGY', value: 'ALLERGY' },
  { label: 'INTOLERANCE', value: 'INTOLERANCE' },
]

const allergyCategoryOptions: Option[] = [
  { label: 'FOOD', value: 'FOOD' },
  { label: 'MEDICATION', value: 'MEDICATION' },
  { label: 'ENVIRONMENT', value: 'ENVIRONMENT' },
  { label: 'BIOLOGIC', value: 'BIOLOGIC' },
]

const allergyCriticalityOptions: Option[] = [
  { label: 'LOW', value: 'LOW' },
  { label: 'HIGH', value: 'HIGH' },
  { label: 'UNABLE_TO_ASSESS', value: 'UNABLE_TO_ASSESS' },
]

const adverseStatusOptions: Option[] = [
  { label: 'IN_PROGRESS', value: 'IN_PROGRESS' },
  { label: 'COMPLETED', value: 'COMPLETED' },
  { label: 'ENTERED_IN_ERROR', value: 'ENTERED_IN_ERROR' },
  { label: 'UNKNOWN', value: 'UNKNOWN' },
]

const adverseActualityOptions: Option[] = [
  { label: 'ACTUAL', value: 'ACTUAL' },
  { label: 'POTENTIAL', value: 'POTENTIAL' },
]

export const patientCreateFields: FieldConfig[] = [
  { key: 'svnr', label: 'SVNR', type: 'text', required: true },
  { key: 'firstName', label: 'First name', type: 'text', required: true },
  { key: 'lastName', label: 'Last name', type: 'text', required: true },
  { key: 'birthDate', label: 'Birth date', type: 'date', required: true },
  { key: 'gender', label: 'Gender', type: 'select', options: genderOptions, required: true },
]

export const patientUpdateFields: FieldConfig[] = [
  { key: 'firstName', label: 'First name', type: 'text' },
  { key: 'lastName', label: 'Last name', type: 'text' },
  { key: 'phone', label: 'Phone', type: 'text' },
  { key: 'email', label: 'Email', type: 'text' },
  { key: 'address', label: 'Address', type: 'textarea', full: true },
]

export const resourceConfigs: ResourceConfig[] = [
  {
    key: 'relatedPersons',
    label: 'Related persons',
    singularLabel: 'related person',
    description: 'Parents and guardians linked to the selected child.',
    searchPath: (patientId) => `/fhir/RelatedPerson?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/related-persons/${id}`,
    createPath: (patientId) => `/api/practitioner/patients/${patientId}/related-persons`,
    updatePath: (id) => `/api/practitioner/related-persons/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'firstName', label: 'First name', type: 'text', required: true },
      { key: 'lastName', label: 'Last name', type: 'text', required: true },
      { key: 'relationship', label: 'Relationship', type: 'text', required: true },
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'email', label: 'Email', type: 'text' },
      { key: 'address', label: 'Address', type: 'textarea', full: true },
    ],
    updateFields: [
      { key: 'phone', label: 'Phone', type: 'text' },
      { key: 'email', label: 'Email', type: 'text' },
      { key: 'address', label: 'Address', type: 'textarea', full: true },
    ],
  },
  {
    key: 'immunizations',
    label: 'Immunizations',
    singularLabel: 'immunization',
    description: 'Completed or planned administered vaccines.',
    searchPath: (patientId) => `/fhir/Immunization?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/immunizations/${id}`,
    createPath: '/api/practitioner/immunizations',
    updatePath: (id) => `/api/practitioner/immunizations/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'status', label: 'Status', type: 'select', options: immunizationStatusOptions, required: true },
      { key: 'vaccineCode', label: 'Vaccine code', type: 'text', required: true },
      { key: 'vaccineDisplay', label: 'Display', type: 'text', required: true },
      { key: 'administrationDate', label: 'Administration date', type: 'date', required: true },
      { key: 'lotNumber', label: 'Lot number', type: 'text' },
      { key: 'site', label: 'Site', type: 'text' },
      { key: 'doseNumber', label: 'Dose number', type: 'number' },
      { key: 'encounterId', label: 'Encounter id', type: 'text' },
    ],
    updateFields: [
      { key: 'lotNumber', label: 'Lot number', type: 'text' },
      { key: 'site', label: 'Site', type: 'text' },
      { key: 'status', label: 'Status', type: 'select', options: immunizationStatusOptions },
      { key: 'notes', label: 'Notes', type: 'textarea', full: true },
    ],
  },
  {
    key: 'recommendations',
    label: 'Recommendations',
    singularLabel: 'recommendation',
    description: 'Suggested or overdue vaccine recommendations.',
    searchPath: (patientId) => `/fhir/ImmunizationRecommendation?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/immunization-recommendations/${id}`,
    createPath: '/api/practitioner/immunization-recommendations',
    updatePath: (id) => `/api/practitioner/immunization-recommendations/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'vaccineCode', label: 'Vaccine code', type: 'text', required: true },
      { key: 'vaccineDisplay', label: 'Display', type: 'text', required: true },
      { key: 'status', label: 'Status', type: 'select', options: recommendationStatusOptions, required: true },
      { key: 'dueDate', label: 'Due date', type: 'date' },
      { key: 'recommendationSource', label: 'Recommendation source', type: 'text' },
    ],
    updateFields: [
      { key: 'dueDate', label: 'Due date', type: 'date' },
      { key: 'status', label: 'Status', type: 'select', options: recommendationStatusOptions },
    ],
  },
  {
    key: 'carePlans',
    label: 'Care plans',
    singularLabel: 'care plan',
    description: 'Vaccination care plans that group next steps and goals.',
    searchPath: (patientId) => [
      `/fhir/CarePlan?patient=${encodeURIComponent(patientId)}`,
      `/fhir/CarePlan?subject=Patient/${encodeURIComponent(patientId)}`,
      `/fhir/CarePlan?subject=${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/care-plans/${id}`,
    createPath: '/api/practitioner/care-plans',
    updatePath: (id) => `/api/practitioner/care-plans/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'title', label: 'Title', type: 'text', required: true },
      { key: 'note', label: 'Note', type: 'textarea', full: true },
      { key: 'startDate', label: 'Start date', type: 'date' },
      { key: 'endDate', label: 'End date', type: 'date' },
      { key: 'status', label: 'Status', type: 'text', required: true },
    ],
    updateFields: [
      { key: 'title', label: 'Title', type: 'text' },
      { key: 'description', label: 'Description', type: 'textarea', full: true },
      { key: 'endDate', label: 'End date', type: 'date' },
      { key: 'status', label: 'Status', type: 'text' },
    ],
  },
  {
    key: 'goals',
    label: 'Goals',
    singularLabel: 'goal',
    description: 'Vaccines left to do inside a care plan.',
    searchPath: (patientId) => [
      `/fhir/Goal?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Goal?subject=Patient/${encodeURIComponent(patientId)}`,
      `/fhir/Goal?subject=${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/goals/${id}`,
    createPath: '/api/practitioner/goals',
    updatePath: (id) => `/api/practitioner/goals/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'lifecycleStatus', label: 'Lifecycle status', type: 'select', options: goalStatusOptions, required: true },
      { key: 'description', label: 'Description', type: 'textarea', required: true, full: true },
      { key: 'targetDueDate', label: 'Target due date', type: 'date' },
      { key: 'startDate', label: 'Start date', type: 'date' },
      { key: 'carePlanId', label: 'Care plan id', type: 'text' },
    ],
    updateFields: [
      { key: 'lifecycleStatus', label: 'Lifecycle status', type: 'select', options: goalStatusOptions, required: true },
      { key: 'description', label: 'Description', type: 'textarea', required: true, full: true },
    ],
  },
  {
    key: 'appointments',
    label: 'Appointments',
    singularLabel: 'appointment',
    description: 'Planned visits for the selected child.',
    searchPath: (patientId) => [
      `/fhir/Appointment?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Appointment?actor=Patient/${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/appointments/${id}`,
    createPath: '/api/practitioner/appointments',
    updatePath: (id) => `/api/practitioner/appointments/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'start', label: 'Start', type: 'datetime', required: true },
      { key: 'end', label: 'End', type: 'datetime' },
      { key: 'reason', label: 'Reason', type: 'text' },
      { key: 'locationId', label: 'Location id', type: 'text' },
    ],
    updateFields: [
      { key: 'start', label: 'Start', type: 'datetime' },
      { key: 'end', label: 'End', type: 'datetime' },
      { key: 'status', label: 'Status', type: 'text' },
    ],
  },
  {
    key: 'encounters',
    label: 'Encounters',
    singularLabel: 'encounter',
    description: 'Clinical encounter snapshots tied to immunizations and observations.',
    searchPath: (patientId) => [
      `/fhir/Encounter?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Encounter?subject=Patient/${encodeURIComponent(patientId)}`,
      `/fhir/Encounter?subject=${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/encounters/${id}`,
    updatePath: (id) => `/api/practitioner/encounters/${id}`,
    canCreate: false,
    canEdit: true,
    updateFields: [
      { key: 'start', label: 'Start', type: 'datetime' },
      { key: 'end', label: 'End', type: 'datetime' },
      { key: 'reason', label: 'Reason', type: 'text' },
      { key: 'location', label: 'Location', type: 'text' },
      { key: 'status', label: 'Status', type: 'text' },
    ],
  },
  {
    key: 'observations',
    label: 'Observations',
    singularLabel: 'observation',
    description: 'Clinical measurements and notes for the selected child.',
    searchPath: (patientId) => [
      `/fhir/Observation?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Observation?subject=Patient/${encodeURIComponent(patientId)}`,
      `/fhir/Observation?subject=${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/observations/${id}`,
    createPath: '/api/practitioner/observations',
    updatePath: (id) => `/api/practitioner/observations/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'display', label: 'Display', type: 'text', required: true },
      { key: 'value', label: 'Value', type: 'text' },
      { key: 'unit', label: 'Unit', type: 'text' },
      { key: 'effectiveDateTime', label: 'Effective date', type: 'datetime' },
      { key: 'encounterId', label: 'Encounter id', type: 'text' },
    ],
    updateFields: [
      { key: 'value', label: 'Value', type: 'text' },
      { key: 'unit', label: 'Unit', type: 'text' },
    ],
  },
  {
    key: 'conditions',
    label: 'Conditions',
    singularLabel: 'condition',
    description: 'Clinical conditions, notes and verification status.',
    searchPath: (patientId) => [
      `/fhir/Condition?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Condition?subject=Patient/${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/conditions/${id}`,
    createPath: '/api/practitioner/conditions',
    updatePath: (id) => `/api/practitioner/conditions/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'display', label: 'Display', type: 'text', required: true },
      { key: 'clinicalStatus', label: 'Clinical status', type: 'text' },
      { key: 'verificationStatus', label: 'Verification status', type: 'text' },
      { key: 'onsetDate', label: 'Onset date', type: 'date' },
      { key: 'notes', label: 'Notes', type: 'textarea', full: true },
    ],
    updateFields: [
      { key: 'clinicalStatus', label: 'Clinical status', type: 'text' },
      { key: 'verificationStatus', label: 'Verification status', type: 'text' },
      { key: 'notes', label: 'Notes', type: 'textarea', full: true },
    ],
  },
  {
    key: 'allergies',
    label: 'Allergies',
    singularLabel: 'allergy',
    description: 'Allergy and intolerance data.',
    searchPath: (patientId) => `/fhir/AllergyIntolerance?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/allergy-intolerances/${id}`,
    createPath: '/api/practitioner/allergy-intolerances',
    updatePath: (id) => `/api/practitioner/allergy-intolerances/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'clinicalStatus', label: 'Clinical status', type: 'select', options: allergyClinicalStatusOptions, required: true },
      { key: 'type', label: 'Type', type: 'select', options: allergyTypeOptions, required: true },
      { key: 'category', label: 'Category', type: 'select', options: allergyCategoryOptions, required: true },
      { key: 'criticality', label: 'Criticality', type: 'select', options: allergyCriticalityOptions, required: true },
      { key: 'code', label: 'Code', type: 'text', required: true },
      { key: 'encounterId', label: 'Encounter id', type: 'text' },
    ],
    updateFields: [
      { key: 'clinicalStatus', label: 'Clinical status', type: 'select', options: allergyClinicalStatusOptions },
      { key: 'criticality', label: 'Criticality', type: 'select', options: allergyCriticalityOptions },
    ],
  },
  {
    key: 'consents',
    label: 'Consents',
    singularLabel: 'consent',
    description: 'Guardian consent history for vaccination decisions.',
    searchPath: (patientId) => [
      `/fhir/Consent?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Consent?subject=Patient/${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/consents/${id}`,
    createPath: '/api/practitioner/consents',
    updatePath: (id) => `/api/practitioner/consents/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'relatedPersonId', label: 'Related person id', type: 'text' },
      { key: 'scope', label: 'Scope', type: 'text', required: true },
      { key: 'status', label: 'Status', type: 'text', required: true },
      { key: 'dateGiven', label: 'Date given', type: 'date' },
    ],
    updateFields: [
      { key: 'status', label: 'Status', type: 'text' },
    ],
  },
  {
    key: 'communications',
    label: 'Communications',
    singularLabel: 'communication',
    description: 'Messages sent to families or linked to recommendations.',
    searchPath: (patientId) => [
      `/fhir/Communication?patient=${encodeURIComponent(patientId)}`,
      `/fhir/Communication?subject=Patient/${encodeURIComponent(patientId)}`,
      `/fhir/Communication?subject=${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/communication/${id}`,
    createPath: '/api/practitioner/communication',
    updatePath: (id) => `/api/practitioner/communication/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'relatedPersonId', label: 'Related person id', type: 'text' },
      { key: 'recommendationId', label: 'Recommendation id', type: 'text' },
      { key: 'medium', label: 'Medium', type: 'text' },
      { key: 'message', label: 'Message', type: 'textarea', full: true },
      { key: 'sentDate', label: 'Sent date', type: 'datetime' },
    ],
    updateFields: [
      { key: 'status', label: 'Status', type: 'text' },
    ],
  },
  {
    key: 'adverseEvents',
    label: 'Adverse events',
    singularLabel: 'adverse event',
    description: 'Follow-up adverse events after vaccination.',
    searchPath: (patientId) => [
      `/fhir/AdverseEvent?subject=Patient/${encodeURIComponent(patientId)}`,
      `/fhir/AdverseEvent?subject=${encodeURIComponent(patientId)}`,
      `/fhir/AdverseEvent?patient=${encodeURIComponent(patientId)}`,
    ],
    getPath: (id) => `/api/practitioner/adverse-events/${id}`,
    createPath: '/api/practitioner/adverse-events',
    updatePath: (id) => `/api/practitioner/adverse-events/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'status', label: 'Status', type: 'select', options: adverseStatusOptions, required: true },
      { key: 'actuality', label: 'Actuality', type: 'select', options: adverseActualityOptions, required: true },
      { key: 'category', label: 'Category', type: 'text' },
      { key: 'recordedDate', label: 'Recorded date', type: 'date' },
      { key: 'encounter', label: 'Encounter id', type: 'text' },
    ],
    updateFields: [
      { key: 'status', label: 'Status', type: 'select', options: adverseStatusOptions },
      { key: 'actuality', label: 'Actuality', type: 'select', options: adverseActualityOptions },
      { key: 'category', label: 'Category', type: 'text' },
      { key: 'recordedDate', label: 'Recorded date', type: 'date' },
      { key: 'encounter', label: 'Encounter id', type: 'text' },
    ],
  },
]

export function titleCase(key: string) {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/[_-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/^./, (char) => char.toUpperCase())
}
