export type VaccineKey =
  | 'INFANT_6IN1'
  | 'RSV_INFANT'
  | 'ROTAVIRUS'
  | 'PNEUMO'
  | 'MMR'
  | 'VARICELLA'
  | 'HPV'
  | 'MENINGO_ACWY'
  | 'MENINGO_B'
  | 'TETANUS'
  | 'TBE'
  | 'INFLUENZA'
  | 'COVID'
  | 'RSV_ADULT'
  | 'ZOSTER'
  | 'HEPA'
  | 'HEPB'

export type RecommendationLevel = 'Routine (many countries)' | 'Risk-based' | 'Varies by country'

export interface VaccineInfo {
  key: VaccineKey
  label: string
  short: string
  protectsAgainst: string
  typicalUse: string
  scheduleNotes: string
  commonSideEffects: string[]
  rareSeriousSideEffects: string[]
  recommendation: RecommendationLevel

  /** Used for matching against FHIR Immunization.vaccineCode display/text. */
  match: RegExp

  /** Optional standardized codings used for robust matching (preferred). */
  codings?: VaccineCoding[]

  /** Optional external references shown in the UI. */
  sources?: VaccineSource[]

  /** Best-effort hint; varies by program/region/insurance. */
  costNotes?: string
}

export interface VaccineCoding {
  system: string
  code: string
  display?: string
}

export interface VaccineSource {
  label: string
  url: string
  note?: string
}

// Notes:
// - This is a *demo* catalogue used for UI hints.
// - Schedules and recommendations vary by country, age, product, and medical history.
// - Prefer codings when present (CVX) and fallback to text matching for demo data.

const CVX = 'http://hl7.org/fhir/sid/cvx'

export const vaccineCatalog: VaccineInfo[] = [
  // ---- Austria/EU childhood programme (broadly) ----
  {
    key: 'INFANT_6IN1',
    label: '6‑in‑1 (DTaP‑IPV‑HepB‑Hib) / Pertussis series',
    short:
      'Combination childhood vaccine that includes pertussis (whooping cough) and other routine protections (6‑in‑1).',
    protectsAgainst:
      'Pertussis, diphtheria, tetanus, polio, hepatitis B, and Hib (product-dependent combinations).',
    typicalUse: 'Routine infant immunisation in many countries.',
    scheduleNotes:
      'Austria (best‑effort): 3 doses in infancy (from ~6 weeks; dose 2 ~2 months later; dose 3 ~6 months after dose 2).',
    commonSideEffects: ['Sore arm', 'Fever', 'Irritability', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Routine (many countries)',
    match: /(6\s*[- ]?in\s*[- ]?1|hexavalent|dtap|dta\s*p|acellular\s*pertussis|whooping\s*cough|pertussis|hib|hepb|ipv)/i,
    codings: [
      // Common CVX codes covering DTaP-containing combos
      { system: CVX, code: '146', display: 'DTaP-HepB-IPV-Hib (hexavalent)' },
      { system: CVX, code: '110', display: 'DTaP-HepB-IPV' },
      { system: CVX, code: '120', display: 'DTaP-Hib-IPV' },
      { system: CVX, code: '130', display: 'DTaP-IPV' },
      { system: CVX, code: '50', display: 'DTaP-Hib' },
      { system: CVX, code: '20', display: 'DTaP' },
    ],
    sources: [
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan', note: 'Austria schedule reference' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
    ],
    costNotes:
      'Austria: routine infant vaccines are generally part of public programmes; funding/availability may vary by Bundesland and programme year.',
  },
  {
    key: 'RSV_INFANT',
    label: 'RSV (infant protection – nirsevimab/Beyfortus)',
    short:
      'Passive protection for infants against RSV, usually offered for the first RSV season (monoclonal antibody, not a traditional vaccine).',
    protectsAgainst: 'Severe RSV lower respiratory tract disease in infants.',
    typicalUse: 'Infants entering their first RSV season; eligibility and timing are seasonal.',
    scheduleNotes:
      'Austria (best‑effort): typically a single dose during the RSV season for eligible infants; timing is seasonal.',
    commonSideEffects: ['Injection-site reactions', 'Mild fever'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(\brsv\b|nirsevimab|beyfortus)/i,
    codings: [
      { system: CVX, code: '306', display: 'Nirsevimab (0.5 mL)' },
      { system: CVX, code: '307', display: 'Nirsevimab (1 mL)' },
    ],
    sources: [
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan', note: 'Austria RSV guidance (seasonal)' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
    ],
    costNotes:
      'Austria: for eligible infants, RSV passive immunisation may be offered under a seasonal programme; details vary by season/programme.',
  },
  {
    key: 'ROTAVIRUS',
    label: 'Rotavirus',
    short: 'Oral vaccine protecting against rotavirus gastroenteritis in infants.',
    protectsAgainst: 'Rotavirus infection (diarrhoea/vomiting) in infants.',
    typicalUse: 'Routine infant vaccination in many countries; product and age window vary.',
    scheduleNotes: 'Given orally in early infancy; product‑specific dose count and maximum ages apply.',
    commonSideEffects: ['Mild diarrhoea', 'Irritability', 'Vomiting (mild)'],
    rareSeriousSideEffects: ['Intussusception (very rare)'],
    recommendation: 'Routine (many countries)',
    match: /(rotavirus|rota)/i,
    codings: [
      { system: CVX, code: '116', display: 'Rotavirus, pentavalent' },
      { system: CVX, code: '119', display: 'Rotavirus, monovalent' },
      { system: CVX, code: '122', display: 'Rotavirus, unspecified formulation' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Rotavirus)', url: 'https://vaccination-info.europa.eu/en/disease/rotavirus-infection' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes:
      'Austria: rotavirus vaccination is commonly included in childhood programmes; age windows apply and implementation can vary.',
  },
  {
    key: 'PNEUMO',
    label: 'Pneumococcal (PCV/PPSV)',
    short: 'Protection against pneumococcal disease; used in infancy and for older/risk groups.',
    protectsAgainst: 'Pneumococcal infections (e.g., pneumonia and invasive disease).',
    typicalUse: 'Routine in infancy in many countries; also recommended for older adults and certain risk groups.',
    scheduleNotes: 'Infant and adult schedules differ by country and product (PCV vs PPSV).',
    commonSideEffects: ['Sore arm', 'Fever', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(pneumococcal|pneumo|pcv|ppsv)/i,
    codings: [
      { system: CVX, code: '133', display: 'Pneumococcal conjugate PCV13' },
      { system: CVX, code: '152', display: 'Pneumococcal conjugate (unspecified)' },
      { system: CVX, code: '215', display: 'PCV15' },
      { system: CVX, code: '216', display: 'PCV20' },
      { system: CVX, code: '33', display: 'PPSV23' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Pneumococcal)', url: 'https://vaccination-info.europa.eu/en/disease/pneumococcal-disease' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
    ],
    costNotes:
      'Austria: pneumococcal vaccination is commonly funded for infants; adult/risk-group programmes vary (some may require co‑payment).',
  },
  {
    key: 'MMR',
    label: 'Measles (MMR)',
    short: 'Protects against measles (often given as MMR: measles, mumps, rubella).',
    protectsAgainst: 'Measles (and usually mumps + rubella when given as combined MMR).',
    typicalUse:
      'Routine childhood vaccination in many countries. Adults without evidence of immunity may be advised to receive 2 doses.',
    scheduleNotes: 'Often a 2‑dose series; exact timing depends on local guidance.',
    commonSideEffects: ['Sore arm', 'Mild fever', 'Mild rash'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Routine (many countries)',
    match: /(\bmmr\b|measles|mumps|rubella)/i,
    codings: [{ system: CVX, code: '03', display: 'MMR' }],
    sources: [
      { label: 'European Vaccination Information Portal (MMR)', url: 'https://vaccination-info.europa.eu/en/vaccination/mmr-vaccines' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes:
      'Austria: MMR is typically included in childhood programmes; catch‑up programmes for adults can vary.',
  },
  {
    key: 'VARICELLA',
    label: 'Varicella (chickenpox)',
    short: 'Protection against varicella; often a 2‑dose series in childhood.',
    protectsAgainst: 'Varicella (chickenpox) and complications.',
    typicalUse: 'Routine childhood vaccination in many countries; adults without immunity may be offered catch‑up.',
    scheduleNotes: 'Commonly 2 doses; timing depends on local guidance and product (Varicella vs MMRV).',
    commonSideEffects: ['Sore arm', 'Mild fever', 'Rash (mild)'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(varicella|chicken\s*pox|mmrv)/i,
    codings: [
      { system: CVX, code: '21', display: 'Varicella' },
      { system: CVX, code: '94', display: 'MMRV' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Varicella)', url: 'https://vaccination-info.europa.eu/en/disease/varicella-chickenpox' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes:
      'Austria: inclusion/funding in programmes can vary by year; check local programme details.',
  },
  {
    key: 'HPV',
    label: 'HPV',
    short: 'Protection against human papillomavirus; prevents several cancers and genital warts.',
    protectsAgainst: 'HPV‑related cancers (e.g., cervical) and genital warts (type-dependent).',
    typicalUse: 'Often recommended in early adolescence; catch‑up policies vary by country and age.',
    scheduleNotes: 'Often 2 doses for younger adolescents and 3 doses for older starters; varies by local guidance.',
    commonSideEffects: ['Sore arm', 'Fever (mild)', 'Dizziness/fainting (observe after injection)'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Routine (many countries)',
    match: /(\bhpv\b|human\s*papilloma)/i,
    codings: [
      { system: CVX, code: '165', display: 'HPV, 9-valent' },
      { system: CVX, code: '62', display: 'HPV, 4-valent' },
      { system: CVX, code: '118', display: 'HPV, 2-valent' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (HPV)', url: 'https://vaccination-info.europa.eu/en/disease/hpv' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes:
      'Austria: HPV is commonly offered via public programmes for certain age groups; catch‑up outside the programme may be self‑pay or subsidised.',
  },
  {
    key: 'MENINGO_ACWY',
    label: 'Meningococcal ACWY',
    short: 'Protection against meningococcal disease caused by serogroups A, C, W, and Y.',
    protectsAgainst: 'Invasive meningococcal disease (meningitis, sepsis).',
    typicalUse: 'Recommended for certain age groups in some countries and for travel/risk-based indications.',
    scheduleNotes: 'Schedules vary by country, age, and product.',
    commonSideEffects: ['Sore arm', 'Fever (mild)', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(meningococcal|meningokokk|\bacwy\b|\bmcvy\b|\bmenacwy\b)/i,
    codings: [
      { system: CVX, code: '114', display: 'Meningococcal ACWY' },
      { system: CVX, code: '136', display: 'Meningococcal conjugate ACWY' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Meningococcal)', url: 'https://vaccination-info.europa.eu/en/disease/meningococcal-disease' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes:
      'Austria: funding depends on programme and age group; check local recommendations and programme coverage.',
  },
  {
    key: 'MENINGO_B',
    label: 'Meningococcal B',
    short: 'Protection against meningococcal serogroup B disease.',
    protectsAgainst: 'Invasive meningococcal disease (meningitis, sepsis).',
    typicalUse: 'Recommended for certain age groups and risk groups in some countries; policies vary.',
    scheduleNotes: 'Schedules vary by country, age, and product.',
    commonSideEffects: ['Sore arm', 'Fever (mild)', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(meningococcal|meningokokk|\bmenb\b|\bmen\s*b\b)/i,
    codings: [
      { system: CVX, code: '162', display: 'Meningococcal B, recombinant' },
      { system: CVX, code: '163', display: 'Meningococcal B, OMV' },
      { system: CVX, code: '203', display: 'Meningococcal B, unspecified' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Meningococcal)', url: 'https://vaccination-info.europa.eu/en/disease/meningococcal-disease' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes:
      'Austria: funding depends on programme and age group; in many settings it can be self‑pay or subsidised.',
  },

  // ---- Boosters / risk-based ----
  {
    key: 'TETANUS',
    label: 'Tetanus (often with diphtheria/pertussis)',
    short: 'Booster protection against tetanus; often combined (Td/Tdap).',
    protectsAgainst: 'Tetanus (often with diphtheria and sometimes pertussis).',
    typicalUse:
      'Routine vaccination; boosters are commonly recommended for ongoing protection, especially after certain injuries.',
    scheduleNotes: 'Boosters are commonly advised about every 10 years (varies by country and situation).',
    commonSideEffects: ['Sore arm', 'Redness/swelling', 'Mild fever', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Routine (many countries)',
    match: /(tetanus|\btdap\b|\btd\b|diphtheria)/i,
    codings: [
      { system: CVX, code: '09', display: 'Td (tetanus and diphtheria toxoids)' },
      { system: CVX, code: '113', display: 'Tdap' },
      { system: CVX, code: '115', display: 'Tdap (adolescent/adult)' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal', url: 'https://vaccination-info.europa.eu/en' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: boosters may be covered in certain programmes; coverage can vary (injury-related boosters may differ).',
  },
  {
    key: 'TBE',
    label: 'Tick‑borne encephalitis (FSME/TBE)',
    short: 'Helps prevent tick‑borne encephalitis; boosters may be needed.',
    protectsAgainst: 'Tick‑borne encephalitis (viral infection transmitted by ticks).',
    typicalUse: 'Risk‑based: endemic areas and frequent outdoor exposure.',
    scheduleNotes:
      'Primary series + boosters. Booster interval varies by age/product (often in the 3–5 year range).',
    commonSideEffects: ['Sore arm', 'Headache', 'Fatigue', 'Mild fever'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Risk-based',
    match: /(\bfsm\b|\bfsm[e]?\b|tick\s*borne\s*encephalitis|\btbe\b)/i,
    codings: [
      { system: CVX, code: '223', display: 'TBE vaccine, paediatric' },
      { system: CVX, code: '224', display: 'TBE vaccine, adult' },
    ],
    sources: [
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
      { label: 'Impfservice Wien (FSME)', url: 'https://impfservice.wien/fsme-zecken-schutzimpfung/' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
    ],
    costNotes: 'Austria: FSME/TBE is commonly self‑pay (sometimes subsidised campaigns); check local offers.',
  },
  {
    key: 'INFLUENZA',
    label: 'Influenza (flu)',
    short: 'Seasonal flu protection; typically repeated each season.',
    protectsAgainst: 'Seasonal influenza strains (changes year to year).',
    typicalUse:
      'Often recommended yearly, especially for older adults, chronic conditions, pregnancy, and healthcare workers.',
    scheduleNotes: 'Typically one dose each flu season (local programmes vary).',
    commonSideEffects: ['Sore arm', 'Mild fever', 'Muscle aches', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(influenza|\bflu\b)/i,
    codings: [
      { system: CVX, code: '140', display: 'Influenza, seasonal, injectable, preservative free' },
      { system: CVX, code: '88', display: 'Influenza, unspecified formulation' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Influenza)', url: 'https://vaccination-info.europa.eu/en/disease/influenza' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: seasonal programmes exist; pricing/coverage can vary by programme and season.',
  },
  {
    key: 'COVID',
    label: 'COVID‑19',
    short: 'Protection against COVID‑19; booster timing varies.',
    protectsAgainst: 'COVID‑19 (SARS‑CoV‑2) and severe disease outcomes.',
    typicalUse: 'Programmes vary widely by country and risk group; boosters may be offered seasonally.',
    scheduleNotes: 'Booster timing depends on local guidance, risk, and vaccine product.',
    commonSideEffects: ['Sore arm', 'Fatigue', 'Headache', 'Fever/chills'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(covid|sars\-?cov\-?2|comirnaty|spikevax)/i,
    codings: [{ system: CVX, code: '500', display: 'SARS-CoV-2 (COVID-19) vaccine, non-US' }],
    sources: [
      { label: 'European Vaccination Information Portal (COVID‑19)', url: 'https://vaccination-info.europa.eu/en/disease/covid-19' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: COVID vaccination/boosters are typically organised through national programmes; eligibility changes over time.',
  },

  // ---- Additional optional entries (for autofill/info) ----
  {
    key: 'RSV_ADULT',
    label: 'RSV (older adult vaccines)',
    short: 'RSV vaccines for older adults; eligibility and recommendations vary.',
    protectsAgainst: 'RSV disease in older adults (vaccine products).',
    typicalUse: 'Often risk/age-based; country programmes vary.',
    scheduleNotes: 'Typically 1 dose; boosters depend on local guidance/product.',
    commonSideEffects: ['Sore arm', 'Fatigue', 'Headache'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(\brsv\b|arexvy|abrysvo|mresvia)/i,
    codings: [
      { system: CVX, code: '303', display: 'RSV vaccine, Arexvy' },
      { system: CVX, code: '305', display: 'RSV vaccine, Abrysvo' },
      { system: CVX, code: '326', display: 'RSV vaccine, mRESVIA' },
    ],
    sources: [
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: adult RSV vaccine availability and funding vary; often self‑pay unless covered for risk groups.',
  },
  {
    key: 'ZOSTER',
    label: 'Shingles (zoster)',
    short: 'Protection against shingles; commonly recommended for older adults in many countries.',
    protectsAgainst: 'Herpes zoster (shingles) and post‑herpetic neuralgia.',
    typicalUse: 'Age/risk-based; policies vary by country.',
    scheduleNotes: 'Often a 2‑dose series for recombinant vaccines; varies by product.',
    commonSideEffects: ['Sore arm', 'Fatigue', 'Fever (mild)'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Varies by country',
    match: /(zoster|shingles|shingrix)/i,
    codings: [
      { system: CVX, code: '187', display: 'Zoster recombinant' },
      { system: CVX, code: '121', display: 'Zoster live' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Shingles)', url: 'https://vaccination-info.europa.eu/en/disease/shingles' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: shingles vaccination is commonly self‑pay or subsidised depending on programmes/insurance.',
  },
  {
    key: 'HEPA',
    label: 'Hepatitis A',
    short: 'Protection against hepatitis A; often travel/risk-based.',
    protectsAgainst: 'Hepatitis A infection.',
    typicalUse: 'Travel and risk-based vaccination; childhood policies vary by country.',
    scheduleNotes: 'Often 2 doses; timing depends on product.',
    commonSideEffects: ['Sore arm', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Risk-based',
    match: /(hepatitis\s*a|\bhep\s*a\b)/i,
    codings: [
      { system: CVX, code: '52', display: 'Hepatitis A, adult' },
      { system: CVX, code: '83', display: 'Hepatitis A, paediatric' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Hepatitis A)', url: 'https://vaccination-info.europa.eu/en/disease/hepatitis' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: hepatitis A is often travel/risk-based and may be self‑pay.',
  },
  {
    key: 'HEPB',
    label: 'Hepatitis B',
    short: 'Protection against hepatitis B; often included in infant combination vaccines.',
    protectsAgainst: 'Hepatitis B infection and chronic liver disease risk.',
    typicalUse: 'Routine in many countries (often part of combination vaccines) and for risk groups.',
    scheduleNotes: 'Often given as part of combination vaccines in infancy; catch-up depends on local guidance.',
    commonSideEffects: ['Sore arm', 'Fatigue'],
    rareSeriousSideEffects: ['Severe allergic reaction (rare)'],
    recommendation: 'Routine (many countries)',
    match: /(hepatitis\s*b|\bhep\s*b\b)/i,
    codings: [
      { system: CVX, code: '08', display: 'Hepatitis B, adolescent or paediatric' },
      { system: CVX, code: '43', display: 'Hepatitis B, adult' },
    ],
    sources: [
      { label: 'European Vaccination Information Portal (Hepatitis B)', url: 'https://vaccination-info.europa.eu/en/disease/hepatitis' },
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
    ],
    costNotes: 'Austria: hepatitis B is usually covered as part of routine childhood programmes (often via combination vaccines).',
  },
]

export function getVaccineInfo(key: VaccineKey): VaccineInfo {
  const v = vaccineCatalog.find((x) => x.key === key)
  if (!v) throw new Error(`Unknown vaccine key: ${key}`)
  return v
}
