export type Language = 'en' | 'de'

type LocalizedText = {
  en: string
  de: string
}

export type Option = { label: LocalizedText; value: string }

export type FieldType = 'text' | 'textarea' | 'date' | 'datetime' | 'number' | 'select'

export type FieldConfig = {
  key: string
  label: LocalizedText
  type: FieldType
  placeholder?: LocalizedText
  required?: boolean
  options?: Option[]
  full?: boolean
}

export type LocalizedFieldConfig = Omit<FieldConfig, 'label' | 'placeholder' | 'options'> & {
  label: string
  placeholder?: string
  options?: { label: string; value: string }[]
}

export type ResourceConfig = {
  key: string
  label: LocalizedText
  singularLabel?: LocalizedText
  description: LocalizedText
  searchPath: (patientId: string) => string | string[]
  getPath: (id: string) => string
  createPath?: string | ((patientId: string) => string)
  updatePath?: (id: string) => string
  createFields?: FieldConfig[]
  updateFields?: FieldConfig[]
  canCreate?: boolean
  canEdit?: boolean
}

export type LocalizedResourceConfig = Omit<ResourceConfig, 'label' | 'singularLabel' | 'description' | 'createFields' | 'updateFields'> & {
  label: string
  singularLabel?: string
  description: string
  createFields?: LocalizedFieldConfig[]
  updateFields?: LocalizedFieldConfig[]
}

function t(en: string, de: string): LocalizedText {
  return { en, de }
}

function localizeText(value: LocalizedText | undefined, language: Language) {
  return value?.[language]
}

function localizeOption(option: Option, language: Language) {
  return {
    ...option,
    label: localizeText(option.label, language) ?? '',
  }
}

function localizeField(field: FieldConfig, language: Language): LocalizedFieldConfig {
  return {
    ...field,
    label: localizeText(field.label, language) ?? '',
    placeholder: localizeText(field.placeholder, language),
    options: field.options?.map((option) => localizeOption(option, language)),
  }
}

function localizeResource(config: ResourceConfig, language: Language): LocalizedResourceConfig {
  return {
    ...config,
    label: localizeText(config.label, language) ?? '',
    singularLabel: localizeText(config.singularLabel, language),
    description: localizeText(config.description, language) ?? '',
    createFields: config.createFields?.map((field) => localizeField(field, language)),
    updateFields: config.updateFields?.map((field) => localizeField(field, language)),
  }
}

const genderOptions: Option[] = [
  { label: t('male', 'männlich'), value: 'male' },
  { label: t('female', 'weiblich'), value: 'female' },
  { label: t('other', 'divers'), value: 'other' },
  { label: t('unknown', 'unbekannt'), value: 'unknown' },
]

const immunizationStatusOptions: Option[] = [
  { label: t('completed', 'abgeschlossen'), value: 'completed' },
  { label: t('entered-in-error', 'irrtümlich erfasst'), value: 'entered_in_error' },
  { label: t('not-done', 'nicht durchgeführt'), value: 'not_done' },
]

const recommendationStatusOptions: Option[] = [
  { label: t('due', 'fällig'), value: 'due' },
  { label: t('completed', 'abgeschlossen'), value: 'completed' },
  { label: t('overdue', 'überfällig'), value: 'overdue' },
]

const goalStatusOptions: Option[] = [
  { label: t('planned', 'geplant'), value: 'planned' },
  { label: t('active', 'aktiv'), value: 'active' },
  { label: t('on-hold', 'pausiert'), value: 'on-hold' },
  { label: t('completed', 'abgeschlossen'), value: 'completed' },
  { label: t('cancelled', 'abgebrochen'), value: 'cancelled' },
]

const allergyClinicalStatusOptions: Option[] = [
  { label: t('ACTIVE', 'AKTIV'), value: 'ACTIVE' },
  { label: t('INACTIVE', 'INAKTIV'), value: 'INACTIVE' },
  { label: t('RESOLVED', 'GELÖST'), value: 'RESOLVED' },
]

const allergyTypeOptions: Option[] = [
  { label: t('ALLERGY', 'ALLERGIE'), value: 'ALLERGY' },
  { label: t('INTOLERANCE', 'INTOLERANZ'), value: 'INTOLERANCE' },
]

const allergyCategoryOptions: Option[] = [
  { label: t('FOOD', 'LEBENSMITTEL'), value: 'FOOD' },
  { label: t('MEDICATION', 'MEDIKAMENT'), value: 'MEDICATION' },
  { label: t('ENVIRONMENT', 'UMWELT'), value: 'ENVIRONMENT' },
  { label: t('BIOLOGIC', 'BIOLOGISCH'), value: 'BIOLOGIC' },
]

const allergyCriticalityOptions: Option[] = [
  { label: t('LOW', 'NIEDRIG'), value: 'LOW' },
  { label: t('HIGH', 'HOCH'), value: 'HIGH' },
  { label: t('UNABLE_TO_ASSESS', 'NICHT BEURTEILBAR'), value: 'UNABLE_TO_ASSESS' },
]

const adverseStatusOptions: Option[] = [
  { label: t('IN_PROGRESS', 'IN BEARBEITUNG'), value: 'IN_PROGRESS' },
  { label: t('COMPLETED', 'ABGESCHLOSSEN'), value: 'COMPLETED' },
  { label: t('ENTERED_IN_ERROR', 'IRRTÜMLICH ERFASST'), value: 'ENTERED_IN_ERROR' },
  { label: t('UNKNOWN', 'UNBEKANNT'), value: 'UNKNOWN' },
]

const adverseActualityOptions: Option[] = [
  { label: t('ACTUAL', 'TATSÄCHLICH'), value: 'ACTUAL' },
  { label: t('POTENTIAL', 'POTENZIELL'), value: 'POTENTIAL' },
]

const patientCreateFields: FieldConfig[] = [
  { key: 'svnr', label: t('SVNR', 'SVNR'), type: 'text', required: true },
  { key: 'firstName', label: t('First name', 'Vorname'), type: 'text', required: true },
  { key: 'lastName', label: t('Last name', 'Nachname'), type: 'text', required: true },
  { key: 'birthDate', label: t('Birth date', 'Geburtsdatum'), type: 'date', required: true },
  { key: 'gender', label: t('Gender', 'Geschlecht'), type: 'select', options: genderOptions, required: true },
]

const patientUpdateFields: FieldConfig[] = [
  { key: 'firstName', label: t('First name', 'Vorname'), type: 'text' },
  { key: 'lastName', label: t('Last name', 'Nachname'), type: 'text' },
  { key: 'phone', label: t('Phone', 'Telefon'), type: 'text' },
  { key: 'email', label: t('Email', 'E-Mail'), type: 'text' },
  { key: 'address', label: t('Address', 'Adresse'), type: 'textarea', full: true },
]

const resourceConfigs: ResourceConfig[] = [
  {
    key: 'relatedPersons',
    label: t('Related persons', 'Bezugs­personen'),
    singularLabel: t('related person', 'Bezugs­person'),
    description: t('Parents and guardians linked to the selected child.', 'Eltern und Obsorgeberechtigte, die mit dem ausgewählten Kind verknüpft sind.'),
    searchPath: (patientId) => `/fhir/RelatedPerson?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/related-persons/${id}`,
    createPath: (patientId) => `/api/practitioner/patients/${patientId}/related-persons`,
    updatePath: (id) => `/api/practitioner/related-persons/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'firstName', label: t('First name', 'Vorname'), type: 'text', required: true },
      { key: 'lastName', label: t('Last name', 'Nachname'), type: 'text', required: true },
      { key: 'relationship', label: t('Relationship', 'Beziehung'), type: 'text', required: true },
      { key: 'phone', label: t('Phone', 'Telefon'), type: 'text' },
      { key: 'email', label: t('Email', 'E-Mail'), type: 'text' },
      { key: 'address', label: t('Address', 'Adresse'), type: 'textarea', full: true },
    ],
    updateFields: [
      { key: 'phone', label: t('Phone', 'Telefon'), type: 'text' },
      { key: 'email', label: t('Email', 'E-Mail'), type: 'text' },
      { key: 'address', label: t('Address', 'Adresse'), type: 'textarea', full: true },
    ],
  },
  {
    key: 'immunizations',
    label: t('Immunizations', 'Impfungen'),
    singularLabel: t('immunization', 'Impfung'),
    description: t('Completed or planned administered vaccines.', 'Abgeschlossene oder geplante verabreichte Impfungen.'),
    searchPath: (patientId) => `/fhir/Immunization?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/immunizations/${id}`,
    createPath: '/api/practitioner/immunizations',
    updatePath: (id) => `/api/practitioner/immunizations/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'status', label: t('Status', 'Status'), type: 'select', options: immunizationStatusOptions, required: true },
      { key: 'vaccineCode', label: t('Vaccine code', 'Impfstoffcode'), type: 'text', required: true },
      { key: 'vaccineDisplay', label: t('Display', 'Bezeichnung'), type: 'text', required: true },
      { key: 'administrationDate', label: t('Administration date', 'Verabreichungsdatum'), type: 'date', required: true },
      { key: 'lotNumber', label: t('Lot number', 'Chargennummer'), type: 'text' },
      { key: 'site', label: t('Site', 'Applikationsstelle'), type: 'text' },
      { key: 'doseNumber', label: t('Dose number', 'Dosisnummer'), type: 'number' },
      { key: 'encounterId', label: t('Encounter id', 'Begegnungs-ID'), type: 'text' },
    ],
    updateFields: [
      { key: 'lotNumber', label: t('Lot number', 'Chargennummer'), type: 'text' },
      { key: 'site', label: t('Site', 'Applikationsstelle'), type: 'text' },
      { key: 'status', label: t('Status', 'Status'), type: 'select', options: immunizationStatusOptions },
      { key: 'notes', label: t('Notes', 'Notizen'), type: 'textarea', full: true },
    ],
  },
  {
    key: 'recommendations',
    label: t('Recommendations', 'Empfehlungen'),
    singularLabel: t('recommendation', 'Empfehlung'),
    description: t('Suggested or overdue vaccine recommendations.', 'Empfohlene oder überfällige Impfempfehlungen.'),
    searchPath: (patientId) => `/fhir/ImmunizationRecommendation?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/immunization-recommendations/${id}`,
    createPath: '/api/practitioner/immunization-recommendations',
    updatePath: (id) => `/api/practitioner/immunization-recommendations/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'vaccineCode', label: t('Vaccine code', 'Impfstoffcode'), type: 'text', required: true },
      { key: 'vaccineDisplay', label: t('Display', 'Bezeichnung'), type: 'text', required: true },
      { key: 'status', label: t('Status', 'Status'), type: 'select', options: recommendationStatusOptions, required: true },
      { key: 'dueDate', label: t('Due date', 'Fälligkeitsdatum'), type: 'date' },
      { key: 'recommendationSource', label: t('Recommendation source', 'Empfehlungsquelle'), type: 'text' },
    ],
    updateFields: [
      { key: 'dueDate', label: t('Due date', 'Fälligkeitsdatum'), type: 'date' },
      { key: 'status', label: t('Status', 'Status'), type: 'select', options: recommendationStatusOptions },
    ],
  },
  {
    key: 'carePlans',
    label: t('Care plans', 'Versorgungspläne'),
    singularLabel: t('care plan', 'Versorgungsplan'),
    description: t('Vaccination care plans that group next steps and goals.', 'Impf-Versorgungspläne, die nächste Schritte und Ziele bündeln.'),
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
      { key: 'title', label: t('Title', 'Titel'), type: 'text', required: true },
      { key: 'note', label: t('Note', 'Notiz'), type: 'textarea', full: true },
      { key: 'startDate', label: t('Start date', 'Startdatum'), type: 'date' },
      { key: 'endDate', label: t('End date', 'Enddatum'), type: 'date' },
      { key: 'status', label: t('Status', 'Status'), type: 'text', required: true },
    ],
    updateFields: [
      { key: 'title', label: t('Title', 'Titel'), type: 'text' },
      { key: 'description', label: t('Description', 'Beschreibung'), type: 'textarea', full: true },
      { key: 'endDate', label: t('End date', 'Enddatum'), type: 'date' },
      { key: 'status', label: t('Status', 'Status'), type: 'text' },
    ],
  },
  {
    key: 'goals',
    label: t('Goals', 'Ziele'),
    singularLabel: t('goal', 'Ziel'),
    description: t('Vaccines left to do inside a care plan.', 'Noch ausstehende Impfungen innerhalb eines Versorgungsplans.'),
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
      { key: 'lifecycleStatus', label: t('Lifecycle status', 'Lebenszyklusstatus'), type: 'select', options: goalStatusOptions, required: true },
      { key: 'description', label: t('Description', 'Beschreibung'), type: 'textarea', required: true, full: true },
      { key: 'targetDueDate', label: t('Target due date', 'Ziel-Fälligkeitsdatum'), type: 'date' },
      { key: 'startDate', label: t('Start date', 'Startdatum'), type: 'date' },
      { key: 'carePlanId', label: t('Care plan id', 'Versorgungsplan-ID'), type: 'text' },
    ],
    updateFields: [
      { key: 'lifecycleStatus', label: t('Lifecycle status', 'Lebenszyklusstatus'), type: 'select', options: goalStatusOptions, required: true },
      { key: 'description', label: t('Description', 'Beschreibung'), type: 'textarea', required: true, full: true },
    ],
  },
  {
    key: 'appointments',
    label: t('Appointments', 'Termine'),
    singularLabel: t('appointment', 'Termin'),
    description: t('Planned visits for the selected child.', 'Geplante Termine für das ausgewählte Kind.'),
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
      { key: 'start', label: t('Start', 'Beginn'), type: 'datetime', required: true },
      { key: 'end', label: t('End', 'Ende'), type: 'datetime' },
      { key: 'reason', label: t('Reason', 'Grund'), type: 'text' },
      { key: 'locationId', label: t('Location id', 'Standort-ID'), type: 'text' },
    ],
    updateFields: [
      { key: 'start', label: t('Start', 'Beginn'), type: 'datetime' },
      { key: 'end', label: t('End', 'Ende'), type: 'datetime' },
      { key: 'status', label: t('Status', 'Status'), type: 'text' },
    ],
  },
  {
    key: 'encounters',
    label: t('Encounters', 'Begegnungen'),
    singularLabel: t('encounter', 'Begegnung'),
    description: t('Clinical encounter snapshots tied to immunizations and observations.', 'Klinische Begegnungen im Zusammenhang mit Impfungen und Beobachtungen.'),
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
      { key: 'start', label: t('Start', 'Beginn'), type: 'datetime' },
      { key: 'end', label: t('End', 'Ende'), type: 'datetime' },
      { key: 'reason', label: t('Reason', 'Grund'), type: 'text' },
      { key: 'location', label: t('Location', 'Ort'), type: 'text' },
      { key: 'status', label: t('Status', 'Status'), type: 'text' },
    ],
  },
  {
    key: 'observations',
    label: t('Observations', 'Beobachtungen'),
    singularLabel: t('observation', 'Beobachtung'),
    description: t('Clinical measurements and notes for the selected child.', 'Klinische Messwerte und Notizen für das ausgewählte Kind.'),
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
      { key: 'code', label: t('Code', 'Code'), type: 'text', required: true },
      { key: 'display', label: t('Display', 'Bezeichnung'), type: 'text', required: true },
      { key: 'value', label: t('Value', 'Wert'), type: 'text' },
      { key: 'unit', label: t('Unit', 'Einheit'), type: 'text' },
      { key: 'effectiveDateTime', label: t('Effective date', 'Wirksamkeitsdatum'), type: 'datetime' },
      { key: 'encounterId', label: t('Encounter id', 'Begegnungs-ID'), type: 'text' },
    ],
    updateFields: [
      { key: 'value', label: t('Value', 'Wert'), type: 'text' },
      { key: 'unit', label: t('Unit', 'Einheit'), type: 'text' },
    ],
  },
  {
    key: 'conditions',
    label: t('Conditions', 'Diagnosen'),
    singularLabel: t('condition', 'Diagnose'),
    description: t('Clinical conditions, notes and verification status.', 'Klinische Diagnosen, Notizen und Verifizierungsstatus.'),
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
      { key: 'code', label: t('Code', 'Code'), type: 'text', required: true },
      { key: 'display', label: t('Display', 'Bezeichnung'), type: 'text', required: true },
      { key: 'clinicalStatus', label: t('Clinical status', 'Klinischer Status'), type: 'text' },
      { key: 'verificationStatus', label: t('Verification status', 'Verifizierungsstatus'), type: 'text' },
      { key: 'onsetDate', label: t('Onset date', 'Beginn-Datum'), type: 'date' },
      { key: 'notes', label: t('Notes', 'Notizen'), type: 'textarea', full: true },
    ],
    updateFields: [
      { key: 'clinicalStatus', label: t('Clinical status', 'Klinischer Status'), type: 'text' },
      { key: 'verificationStatus', label: t('Verification status', 'Verifizierungsstatus'), type: 'text' },
      { key: 'notes', label: t('Notes', 'Notizen'), type: 'textarea', full: true },
    ],
  },
  {
    key: 'allergies',
    label: t('Allergies', 'Allergien'),
    singularLabel: t('allergy', 'Allergie'),
    description: t('Allergy and intolerance data.', 'Allergie- und Intoleranzdaten.'),
    searchPath: (patientId) => `/fhir/AllergyIntolerance?patient=${encodeURIComponent(patientId)}`,
    getPath: (id) => `/api/practitioner/allergy-intolerances/${id}`,
    createPath: '/api/practitioner/allergy-intolerances',
    updatePath: (id) => `/api/practitioner/allergy-intolerances/${id}`,
    canCreate: true,
    canEdit: true,
    createFields: [
      { key: 'clinicalStatus', label: t('Clinical status', 'Klinischer Status'), type: 'select', options: allergyClinicalStatusOptions, required: true },
      { key: 'type', label: t('Type', 'Typ'), type: 'select', options: allergyTypeOptions, required: true },
      { key: 'category', label: t('Category', 'Kategorie'), type: 'select', options: allergyCategoryOptions, required: true },
      { key: 'criticality', label: t('Criticality', 'Kritikalität'), type: 'select', options: allergyCriticalityOptions, required: true },
      { key: 'code', label: t('Code', 'Code'), type: 'text', required: true },
      { key: 'encounterId', label: t('Encounter id', 'Begegnungs-ID'), type: 'text' },
    ],
    updateFields: [
      { key: 'clinicalStatus', label: t('Clinical status', 'Klinischer Status'), type: 'select', options: allergyClinicalStatusOptions },
      { key: 'criticality', label: t('Criticality', 'Kritikalität'), type: 'select', options: allergyCriticalityOptions },
    ],
  },
  {
    key: 'consents',
    label: t('Consents', 'Einwilligungen'),
    singularLabel: t('consent', 'Einwilligung'),
    description: t('Guardian consent history for vaccination decisions.', 'Einwilligungsverlauf der Sorgeberechtigten für Impfentscheidungen.'),
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
      { key: 'relatedPersonId', label: t('Related person id', 'Bezugs­personen-ID'), type: 'text' },
      { key: 'scope', label: t('Scope', 'Geltungsbereich'), type: 'text', required: true },
      { key: 'status', label: t('Status', 'Status'), type: 'text', required: true },
      { key: 'dateGiven', label: t('Date given', 'Erteilungsdatum'), type: 'date' },
    ],
    updateFields: [
      { key: 'status', label: t('Status', 'Status'), type: 'text' },
    ],
  },
  {
    key: 'communications',
    label: t('Communications', 'Mitteilungen'),
    singularLabel: t('communication', 'Mitteilung'),
    description: t('Messages sent to families or linked to recommendations.', 'Nachrichten an Familien oder im Zusammenhang mit Empfehlungen.'),
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
      { key: 'relatedPersonId', label: t('Related person id', 'Bezugs­personen-ID'), type: 'text' },
      { key: 'recommendationId', label: t('Recommendation id', 'Empfehlungs-ID'), type: 'text' },
      { key: 'medium', label: t('Medium', 'Medium'), type: 'text' },
      { key: 'message', label: t('Message', 'Nachricht'), type: 'textarea', full: true },
      { key: 'sentDate', label: t('Sent date', 'Sendedatum'), type: 'datetime' },
    ],
    updateFields: [
      { key: 'status', label: t('Status', 'Status'), type: 'text' },
    ],
  },
  {
    key: 'adverseEvents',
    label: t('Adverse events', 'Nebenwirkungen'),
    singularLabel: t('adverse event', 'Nebenwirkung'),
    description: t('Follow-up adverse events after vaccination.', 'Nachverfolgte unerwünschte Ereignisse nach einer Impfung.'),
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
      { key: 'status', label: t('Status', 'Status'), type: 'select', options: adverseStatusOptions, required: true },
      { key: 'actuality', label: t('Actuality', 'Tatsächlichkeit'), type: 'select', options: adverseActualityOptions, required: true },
      { key: 'category', label: t('Category', 'Kategorie'), type: 'text' },
      { key: 'recordedDate', label: t('Recorded date', 'Erfassungsdatum'), type: 'date' },
      { key: 'encounter', label: t('Encounter id', 'Begegnungs-ID'), type: 'text' },
    ],
    updateFields: [
      { key: 'status', label: t('Status', 'Status'), type: 'select', options: adverseStatusOptions },
      { key: 'actuality', label: t('Actuality', 'Tatsächlichkeit'), type: 'select', options: adverseActualityOptions },
      { key: 'category', label: t('Category', 'Kategorie'), type: 'text' },
      { key: 'recordedDate', label: t('Recorded date', 'Erfassungsdatum'), type: 'date' },
      { key: 'encounter', label: t('Encounter id', 'Begegnungs-ID'), type: 'text' },
    ],
  },
]

export function getPatientCreateFields(language: Language = 'en') {
  return patientCreateFields.map((field) => localizeField(field, language))
}

export function getPatientUpdateFields(language: Language = 'en') {
  return patientUpdateFields.map((field) => localizeField(field, language))
}

export function getResourceConfigs(language: Language = 'en') {
  return resourceConfigs.map((config) => localizeResource(config, language))
}

export function titleCase(key: string) {
  return key
    .replace(/([A-Z])/g, ' $1')
    .replace(/[_-]/g, ' ')
    .replace(/\s+/g, ' ')
    .trim()
    .replace(/^./, (char) => char.toUpperCase())
}
