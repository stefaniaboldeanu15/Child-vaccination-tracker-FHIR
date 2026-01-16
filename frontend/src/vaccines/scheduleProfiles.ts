// Project scope: Europe-focused with a main focus on Austria.
// Keep a generic "GLOBAL" profile as a fallback, but do not expose US/ACIP-specific engines.
export type VaccineScheduleProfile = 'GLOBAL' | 'AUSTRIA'

export interface ScheduleProfileInfo {
  key: VaccineScheduleProfile
  label: string
  description: string
  references: { label: string; url: string }[]
}

export const scheduleProfiles: ScheduleProfileInfo[] = [
  {
    key: 'GLOBAL',
    label: 'Global (generic)',
    description: 'Generic reminders. Schedules vary by country and personal risk.',
    references: [
      { label: 'European Vaccination Information Portal (EU)', url: 'https://vaccination-info.europa.eu/en' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'WHO – Vaccines and immunization', url: 'https://www.who.int/health-topics/vaccines-and-immunization' },
    ],
  },
  {
    key: 'AUSTRIA',
    label: 'Austria (Impfplan)',
    description: 'Uses Austrian schedule hints where implemented (best-effort).',
    references: [
      { label: 'Impfplan Österreich (Sozialministerium)', url: 'https://www.sozialministerium.gv.at/impfplan' },
      { label: 'ECDC Vaccine Scheduler (EU/EEA)', url: 'https://vaccine-schedule.ecdc.europa.eu/' },
      { label: 'Impfservice Wien (example local guidance)', url: 'https://impfservice.wien/' },
    ],
  },
]

export function inferProfileFromCountry(country?: string | null): VaccineScheduleProfile {
  const c = (country ?? '').trim().toLowerCase()
  // Requirement: infer schedule per patient by country, defaulting to Austria when unknown.
  if (!c) return 'AUSTRIA'

  if (c === 'at' || c === 'aut' || c.includes('austria') || c.includes('österreich') || c.includes('oesterreich')) {
    return 'AUSTRIA'
  }

  // For other countries we currently do not have a curated local schedule table.
  // Default to Austria (per requirement) rather than a generic profile.
  return 'AUSTRIA'
}

const STORAGE_KEY_PREFIX = 'vax_registry_profile:'

export function readScheduleProfileFromStorage(patientId?: string | null): VaccineScheduleProfile | null {
  try {
    const key = `${STORAGE_KEY_PREFIX}${patientId ?? 'global'}`
    const raw = window.localStorage.getItem(key)
    return raw === 'AUSTRIA' || raw === 'GLOBAL' ? raw : null
  } catch {
    return null
  }
}

export function writeScheduleProfileToStorage(patientId: string | null | undefined, profile: VaccineScheduleProfile) {
  try {
    const key = `${STORAGE_KEY_PREFIX}${patientId ?? 'global'}`
    window.localStorage.setItem(key, profile)
  } catch {
    // ignore
  }
}
