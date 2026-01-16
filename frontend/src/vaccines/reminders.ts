import type { VaccineKey, VaccineInfo } from './vaccineCatalog'
import { vaccineCatalog } from './vaccineCatalog'
import type { VaccineScheduleProfile } from './scheduleProfiles'
import { inferProfileFromCountry } from './scheduleProfiles'

export type ReminderStatus = 'missing' | 'due' | 'due-soon' | 'up-to-date' | 'unknown'

export interface VaccineReminder {
  key: VaccineKey
  status: ReminderStatus
  title: string
  message: string
  lastDoseDate?: string
  dosesRecorded?: number

  /**
   * Label shown in UIs (e.g. "3 months" or "11–12 years").
   * Best-effort and depends on schedule profile.
   */
  recommendedAgeLabel?: string

  /** Best-effort schedule calculation inputs/outputs (used for UI detail views). */
  scheduleProfile?: VaccineScheduleProfile
  patientAgeYears?: number | null
  intervalYears?: number
  nextDueDate?: string

  /** For multi-dose series (best-effort). */
  nextDoseNumber?: number
  seriesTargetDoses?: number
  earliestDueDate?: string
}

export interface VaccinationLike {
  vaccineName?: string
  vaccineType?: string
  vaccineSystem?: string
  vaccineCode?: string
  date?: string
  status?: 'completed' | 'scheduled' | string
}

function yearsBetween(fromIso: string, to: Date): number {
  const from = new Date(fromIso)
  const ms = to.getTime() - from.getTime()
  return ms / (1000 * 60 * 60 * 24 * 365.25)
}

function addYearsIso(dateIso: string, years: number): string | null {
  const d = new Date(dateIso)
  if (Number.isNaN(d.getTime())) return null
  const copy = new Date(d.getTime())
  copy.setFullYear(copy.getFullYear() + Math.round(years))
  return copy.toISOString().slice(0, 10)
}

function addDaysIso(dateIso: string, days: number): string | null {
  const d = new Date(dateIso)
  if (Number.isNaN(d.getTime())) return null
  const copy = new Date(d.getTime())
  copy.setDate(copy.getDate() + days)
  return copy.toISOString().slice(0, 10)
}

function addMonthsIso(dateIso: string, months: number): string | null {
  const d = new Date(dateIso)
  if (Number.isNaN(d.getTime())) return null
  const copy = new Date(d.getTime())
  copy.setMonth(copy.getMonth() + months)
  return copy.toISOString().slice(0, 10)
}

function laterIso(a: string | null, b: string | null): string | null {
  if (!a) return b
  if (!b) return a
  return a >= b ? a : b
}

function daysUntil(iso: string, now = new Date()): number | null {
  const d = new Date(iso)
  if (Number.isNaN(d.getTime())) return null
  return (d.getTime() - now.getTime()) / (1000 * 60 * 60 * 24)
}

function ageDays(birthDateIso?: string | null, now = new Date()): number | null {
  const bd = (birthDateIso ?? '').trim()
  if (!bd) return null
  const d = new Date(bd)
  if (Number.isNaN(d.getTime())) return null
  return (now.getTime() - d.getTime()) / (1000 * 60 * 60 * 24)
}

function fmtAgeMonths(months: number): string {
  if (!Number.isFinite(months)) return ''
  if (months < 12) return `${Math.round(months)} months`
  const years = months / 12
  if (years < 10) return `${Math.round(years * 10) / 10} years`
  return `${Math.round(years)} years`
}

function fmtDate(iso: string): string {
  try {
    return new Date(iso).toLocaleDateString('en-GB')
  } catch {
    return iso
  }
}

function fmtAgeLabelFromMonths(months: number): string {
  if (!Number.isFinite(months) || months < 0) return ''
  if (months < 12) return `${Math.round(months)} months`
  const yrs = months / 12
  if (Math.abs(yrs - Math.round(yrs)) < 0.05) return `${Math.round(yrs)} years`
  return `${yrs.toFixed(1)} years`
}

function ageYears(birthDateIso?: string | null, now = new Date()): number | null {
  const bd = (birthDateIso ?? '').trim()
  if (!bd) return null
  const d = new Date(bd)
  if (Number.isNaN(d.getTime())) return null
  const yrs = yearsBetween(d.toISOString().slice(0, 10), now)
  return Number.isFinite(yrs) ? yrs : null
}

function matchesCatalogByCoding(v: VaccinationLike, info: VaccineInfo): boolean {
  const sys = (v.vaccineSystem ?? '').trim()
  const code = (v.vaccineCode ?? '').trim()
  if (!sys || !code) return false
  const codings = info.codings ?? []
  return codings.some((c) => c.system === sys && c.code === code)
}

function matchVaccineKey(v: VaccinationLike): VaccineKey | null {
  // Prefer code matching.
  for (const info of vaccineCatalog) {
    if (matchesCatalogByCoding(v, info)) return info.key
  }

  // Fallback: regex on name/type.
  const hay = `${v.vaccineName ?? ''} ${v.vaccineType ?? ''}`.toLowerCase()
  const info = vaccineCatalog.find((c) => c.match.test(hay))
  return info ? info.key : null
}

function mostRecentCompleted(v: VaccinationLike[]): string | undefined {
  const completed = v
    .filter((x) => String(x.status).toLowerCase() === 'completed')
    .map((x) => (typeof x.date === 'string' ? x.date : ''))
    .filter(Boolean)
    .sort()
  return completed.length ? completed[completed.length - 1] : undefined
}

function countCompleted(v: VaccinationLike[]): number {
  return v.filter((x) => String(x.status).toLowerCase() === 'completed').length
}

export function computeVaccineReminders(args: {
  vaccinations: VaccinationLike[]
  patientBirthDate?: string | null
  patientCountry?: string | null
  scheduleProfile?: VaccineScheduleProfile
  /** If true, include travel/optional vaccines (e.g., Hepatitis A) even when not recorded yet. */
  includeTravelOptional?: boolean
}): VaccineReminder[] {
  const now = new Date()

  const profile: VaccineScheduleProfile =
    args.scheduleProfile ?? inferProfileFromCountry(args.patientCountry)
  const patientAge = ageYears(args.patientBirthDate ?? null, now)

  // Group vaccinations by catalogue key (prefer coding match, fallback to name/type text).
  const grouped: Record<string, VaccinationLike[]> = {}
  for (const vac of args.vaccinations) {
    const key = matchVaccineKey(vac)
    if (!key) continue
    grouped[key] ||= []
    grouped[key].push(vac)
  }

  const out: VaccineReminder[] = []

  // Infant 6-in-1 / pertussis-containing primary series (Austria schedule best-effort).
  {
    const key: VaccineKey = 'INFANT_6IN1'
    const seriesTargetDoses = 3

    const bd = (args.patientBirthDate ?? '').trim() || null
    const aDays = ageDays(bd, now)

    // Only show for infants/young children; if we can't compute age, show "check".
    if (aDays === null) {
      out.push({
        key,
        status: 'unknown',
        title: 'Pertussis / 6‑in‑1 infant series',
        message:
          'Birth date is not available, so the infant schedule cannot be calculated. If this patient is an infant, verify whether the 3-dose primary series has been started/completed per local guidance.',
        scheduleProfile: profile,
        patientAgeYears: patientAge,
        seriesTargetDoses,
      })
    } else if (aDays <= 730) {
      // Apply schedule logic up to ~2 years to keep it focused.
      const doses = countCompleted(grouped[key] ?? [])
      const last = mostRecentCompleted(grouped[key] ?? [])

      if (doses >= seriesTargetDoses) {
        out.push({
          key,
          status: 'up-to-date',
          title: 'Pertussis / 6‑in‑1 infant series',
          message: `At least ${seriesTargetDoses} doses are recorded for a pertussis-containing childhood vaccine in this registry (best-effort matching).`,
          dosesRecorded: doses,
          lastDoseDate: last,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
          seriesTargetDoses,
          recommendedAgeLabel: profile === 'AUSTRIA' ? '3, 5, 11–12 months' : 'Infant series (varies)',
        })
      } else {
        const nextDoseNumber = doses + 1

        // Austria 2+1 schedule (best-effort) based on national guidance:
        // - Dose 1: 3rd month (can start from 6 weeks)
        // - Dose 2: 5th month
        // - Dose 3: 11th-12th month
        let dueDate: string | null = null
        let earliest: string | null = null
        let recommendedAgeLabel: string | undefined

        if (!bd) {
          // Should not happen (aDays computed), but keep safe.
          dueDate = null
        } else if (doses === 0) {
          earliest = addDaysIso(bd, 42)
          dueDate = addMonthsIso(bd, 3)
          recommendedAgeLabel = profile === 'AUSTRIA' ? '3 months (earliest 6 weeks)' : '2 months (varies by country)'
        } else if (doses === 1 && last && bd) {
          // Dose 2: around the 5th month; keep a minimum spacing.
          const byAge = addMonthsIso(bd, 5)
          const byMin = addDaysIso(last, 56)
          dueDate = laterIso(byAge, byMin)
          earliest = byMin
          recommendedAgeLabel = profile === 'AUSTRIA' ? '5 months' : '4 months (varies by country)'
        } else if (doses === 2 && last && bd) {
          // Dose 3: 11th-12th month; keep a minimum spacing (~6 months after dose 2).
          const byAge = addMonthsIso(bd, 11)
          const byMin = addMonthsIso(last, 6)
          dueDate = laterIso(byAge, byMin)
          earliest = byMin
          recommendedAgeLabel = profile === 'AUSTRIA' ? '11–12 months' : '12 months (varies by country)'
        }

        const soonWindowDays = 21
        const until = dueDate ? daysUntil(dueDate, now) : null
        const status: ReminderStatus = (() => {
          if (!dueDate || until === null) return 'unknown'
          if (until <= 0) return doses === 0 ? 'due' : 'due'
          if (until <= soonWindowDays) return 'due-soon'
          return 'up-to-date'
        })()

        const doseLabel = `Dose ${nextDoseNumber} of ${seriesTargetDoses}`
        const scheduleHint =
          profile === 'AUSTRIA'
            ? 'Austria (best‑effort): 2+1 schedule in the 3rd, 5th and 11th–12th month (minimum spacing applies).'
            : 'Infant schedules vary by country and product.'

        const msgParts: string[] = []
        msgParts.push(`${doseLabel}. ${scheduleHint}`)
        if (last) msgParts.push(`Last recorded dose: ${fmtDate(last)}.`)
        if (earliest) msgParts.push(`Earliest (min interval) date: ${fmtDate(earliest)}.`)
        if (dueDate) msgParts.push(`Next dose due around: ${fmtDate(dueDate)}.`)
        if (status === 'due') msgParts.push('Status: due (best-effort calculation).')
        if (status === 'due-soon') msgParts.push('Status: due soon (best-effort calculation).')
        if (status === 'up-to-date') msgParts.push('Status: not due yet (best-effort calculation).')

        out.push({
          key,
          status,
          title: 'Pertussis / 6‑in‑1 infant series',
          message: msgParts.join(' '),
          dosesRecorded: doses,
          lastDoseDate: last,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
          nextDueDate: dueDate ?? undefined,
          earliestDueDate: earliest ?? undefined,
          nextDoseNumber,
          seriesTargetDoses,
          recommendedAgeLabel,
        })
      }
    }
  }

  // RSV infant (nirsevimab) – show only for < 12 months.
  {
    const key: VaccineKey = 'RSV_INFANT'
    const bd = (args.patientBirthDate ?? '').trim() || null
    const aDays = ageDays(bd, now)
    if (aDays === null) {
      out.push({
        key,
        status: 'unknown',
        title: 'RSV (infant protection)',
        message:
          'Birth date is missing, so age-based RSV infant protection cannot be evaluated. RSV infant programmes are typically limited to the first RSV season / first year of life.',
        scheduleProfile: profile,
        patientAgeYears: patientAge,
      })
    } else if (aDays < 365) {
      const doses = countCompleted(grouped[key] ?? [])
      const last = mostRecentCompleted(grouped[key] ?? [])
      if (doses >= 1) {
        out.push({
          key,
          status: 'up-to-date',
          title: 'RSV (infant protection)',
          message: 'An RSV infant protection dose (nirsevimab/Beyfortus) is recorded in this registry.',
          dosesRecorded: doses,
          lastDoseDate: last,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
        })
      } else {
        const m = now.getMonth() + 1
        const inSeason = m >= 10 || m <= 3

        // If not in season, suggest next Oct 1 as a planning anchor.
        let nextSeasonIso: string | null = null
        if (!inSeason) {
          const y = now.getFullYear()
          const targetYear = m < 10 ? y : y + 1
          nextSeasonIso = `${targetYear}-10-01`
        }

        const status: ReminderStatus = inSeason ? 'due' : 'due-soon'
        const msg = inSeason
          ? 'No RSV infant protection is recorded. RSV programmes are seasonal; if the infant is eligible, it is typically given before or during the RSV season (best-effort).'
          : `No RSV infant protection is recorded. RSV programmes are seasonal; consider planning for the next RSV season (around ${nextSeasonIso ? fmtDate(nextSeasonIso) : 'Oct'}; best-effort).`

        out.push({
          key,
          status,
          title: 'RSV (infant protection)',
          message: msg,
          dosesRecorded: doses,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
          nextDueDate: nextSeasonIso ?? undefined,
        })
      }
    }
  }

  // MMR: incomplete if <2 doses recorded.
  {
    const key: VaccineKey = 'MMR'
    const doses = countCompleted(grouped[key] ?? [])
    if (doses === 0) {
      out.push({
        key,
        status: 'missing',
        title: 'Measles (MMR)',
        message:
          'No MMR/measles vaccination is recorded. If you are not sure you had 2 doses, discuss catch‑up vaccination with your clinician.',
        dosesRecorded: doses,
      })
    } else if (doses < 2) {
      out.push({
        key,
        status: 'due',
        title: 'Measles (MMR)',
        message:
          'Only one dose is recorded. Many programs consider 2 doses needed for full protection; confirm whether you need a second dose.',
        dosesRecorded: doses,
        lastDoseDate: mostRecentCompleted(grouped[key] ?? []),
      })
    } else {
      out.push({
        key,
        status: 'up-to-date',
        title: 'Measles (MMR)',
        message: 'At least two doses are recorded in this registry.',
        dosesRecorded: doses,
        lastDoseDate: mostRecentCompleted(grouped[key] ?? []),
      })
    }
  }

  // Tetanus: booster hint based on last recorded dose.
  {
    const key: VaccineKey = 'TETANUS'
    const last = mostRecentCompleted(grouped[key] ?? [])
    const older = profile === 'AUSTRIA' && typeof patientAge === 'number' && patientAge >= 60
    const interval = profile === 'AUSTRIA' ? (older ? 5 : 10) : 10
    const dueSoonAfter = profile === 'AUSTRIA' ? Math.max(interval - 1, 1) : Math.max(interval - 3, 1)

    if (!last) {
      out.push({
        key,
        status: 'missing',
        title: 'Tetanus booster (Td/Tdap)',
        message:
          profile === 'AUSTRIA'
            ? `No tetanus/Td/Tdap vaccination is recorded. In Austria, boosters are commonly advised about every ${older ? '5' : '10'} years${older ? ' for people aged 60+' : ''} (best‑effort summary).`
            : 'No tetanus/Td/Tdap vaccination is recorded. Boosters are commonly advised about every ~10 years (varies by country and situation).',
        scheduleProfile: profile,
        patientAgeYears: patientAge,
        intervalYears: interval,
      })
    } else {
      const yrs = yearsBetween(last, now)

      const dueDate = addYearsIso(last, interval)
      const status: ReminderStatus = yrs >= interval ? 'due' : yrs >= dueSoonAfter ? 'due-soon' : 'up-to-date'

      out.push({
        key,
        status,
        title: 'Tetanus booster (Td/Tdap)',
        message: (() => {
          if (!dueDate) {
            return status === 'due'
              ? `Your last recorded dose is over ~${interval} years ago. Booster intervals vary; check local guidance.`
              : status === 'due-soon'
                ? `Your last recorded dose is approaching ~${interval} years. Consider checking whether a booster is recommended for you.`
                : 'A recent tetanus/Td/Tdap dose is recorded.'
          }

          if (status === 'due') {
            return `Overdue: last recorded dose ${fmtDate(last)}. Next booster was due around ${fmtDate(dueDate)} (interval ~${interval} years${profile === 'AUSTRIA' ? (older ? ', 60+ profile' : ', Austria profile') : ''}).`
          }
          if (status === 'due-soon') {
            return `Due soon: last recorded dose ${fmtDate(last)}. Next booster is due around ${fmtDate(dueDate)} (interval ~${interval} years${profile === 'AUSTRIA' ? (older ? ', 60+ profile' : ', Austria profile') : ''}).`
          }
          return `Up to date: last recorded dose ${fmtDate(last)}. Next booster due around ${fmtDate(dueDate)} (interval ~${interval} years${profile === 'AUSTRIA' ? (older ? ', 60+ profile' : ', Austria profile') : ''}).`
        })(),
        lastDoseDate: last,
        scheduleProfile: profile,
        patientAgeYears: patientAge,
        intervalYears: interval,
        nextDueDate: dueDate ?? undefined,
      })
    }
  }

  // TBE: renewal hint.
  {
    const key: VaccineKey = 'TBE'
    const last = mostRecentCompleted(grouped[key] ?? [])
    const doses = countCompleted(grouped[key] ?? [])

    const older = profile === 'AUSTRIA' && typeof patientAge === 'number' && patientAge >= 60
    const interval = profile === 'AUSTRIA' ? (older ? 3 : 5) : 5
    const dueSoonAfter = Math.max(interval - 1, 1)

    if (!last) {
      out.push({
        key,
        status: 'unknown',
        title: 'Tick‑borne encephalitis (FSME/TBE)',
        message:
          profile === 'AUSTRIA'
            ? `No FSME/TBE vaccination is recorded. In Austria, boosters after completing the primary series are often around every ${interval} years${older ? ' for people aged 60+' : ''} (best‑effort summary).`
            : `No FSME/TBE vaccination is recorded. If you spend time in tick‑endemic areas, ask about vaccination. Boosters after a primary series are often in the 3–5 year range (varies by country/product).`,
        dosesRecorded: doses,
        scheduleProfile: profile,
        patientAgeYears: patientAge,
        intervalYears: interval,
      })
    } else {
      const yrs = yearsBetween(last, now)

      const dueDate = addYearsIso(last, interval)
      const status: ReminderStatus = yrs >= interval ? 'due' : yrs >= dueSoonAfter ? 'due-soon' : 'up-to-date'

      const seriesNote = doses > 0 && doses < 3
        ? ' (Note: some products use a multi-dose primary series; earlier boosters can be shorter.)'
        : ''

      out.push({
        key,
        status,
        title: 'Tick‑borne encephalitis (FSME/TBE)',
        message: (() => {
          if (!dueDate) {
            return status === 'due'
              ? `Your last recorded FSME/TBE dose is over ~${interval} years ago. Booster intervals vary; check local guidance.${seriesNote}`
              : status === 'due-soon'
                ? `Your last recorded FSME/TBE dose is approaching ~${interval} years. Booster intervals vary; check local guidance.${seriesNote}`
                : `A recent FSME/TBE dose is recorded.${seriesNote}`
          }

          if (status === 'due') {
            return `Overdue: last recorded dose ${fmtDate(last)}. Next booster was due around ${fmtDate(dueDate)} (interval ~${interval} years${profile === 'AUSTRIA' ? (older ? ', 60+ profile' : ', Austria profile') : ''}).${seriesNote}`
          }
          if (status === 'due-soon') {
            return `Due soon: last recorded dose ${fmtDate(last)}. Next booster is due around ${fmtDate(dueDate)} (interval ~${interval} years${profile === 'AUSTRIA' ? (older ? ', 60+ profile' : ', Austria profile') : ''}).${seriesNote}`
          }
          return `Up to date: last recorded dose ${fmtDate(last)}. Next booster due around ${fmtDate(dueDate)} (interval ~${interval} years${profile === 'AUSTRIA' ? (older ? ', 60+ profile' : ', Austria profile') : ''}).${seriesNote}`
        })(),
        lastDoseDate: last,
        dosesRecorded: doses,
        scheduleProfile: profile,
        patientAgeYears: patientAge,
        intervalYears: interval,
        nextDueDate: dueDate ?? undefined,
      })
    }
  }

  // Influenza: seasonal (typically yearly, autumn/winter).
  // Best-effort for EU/Austria: treat the "current season" as starting Aug 1.
  {
    const key: VaccineKey = 'INFLUENZA'
    const last = mostRecentCompleted(grouped[key] ?? [])

    // Aug..Dec => season starts this year; Jan..Jul => season started previous year.
    const seasonYear = now.getMonth() >= 7 ? now.getFullYear() : now.getFullYear() - 1
    const seasonStart = new Date(Date.UTC(seasonYear, 7, 1)) // Aug 1

    const isInCurrentSeason = (iso: string) => {
      const d = new Date(iso)
      if (Number.isNaN(d.getTime())) return false
      return d.getTime() >= seasonStart.getTime()
    }

    if (!last) {
      out.push({
        key,
        status: 'unknown',
        title: 'Influenza (flu)',
        message:
          'Flu vaccines are typically offered each season, especially for people at higher risk. If relevant to you, ask about the current season’s vaccine.',
      })
    } else {
      const ok = (() => {
        const d = new Date(last)
        if (Number.isNaN(d.getTime())) return null
        return d.getTime() >= seasonStart.getTime()
      })()

      out.push({
        key,
        status: ok === null ? 'unknown' : ok ? 'up-to-date' : 'due',
        title: 'Influenza (flu)',
        message:
          ok === null
            ? 'A flu vaccine is recorded, but the date could not be interpreted reliably.'
            : ok
              ? 'A flu vaccine for the current season is recorded.'
              : 'Your last recorded flu vaccine is from a previous season. Flu vaccination is typically repeated each season.',
        lastDoseDate: last,
      })
    }
  }

  // COVID: general hint.
  {
    const key: VaccineKey = 'COVID'
    const last = mostRecentCompleted(grouped[key] ?? [])
    if (!last) {
      out.push({
        key,
        status: 'unknown',
        title: 'COVID‑19',
        message:
          'COVID‑19 booster programs vary by country and risk group. If you are in a recommended group, ask about the latest booster guidance.',
      })
    } else {
      const yrs = yearsBetween(last, now)
      out.push({
        key,
        status: yrs >= 1 ? 'due-soon' : 'up-to-date',
        title: 'COVID‑19',
        message:
          yrs >= 1
            ? 'Your last recorded COVID‑19 dose is over ~12 months ago. Some programs offer boosters seasonally or for higher‑risk people.'
            : 'A recent COVID‑19 dose is recorded.',
        lastDoseDate: last,
      })
    }
  }

  // Hepatitis A: travel/risk-based. Do NOT show in routine reminders unless recorded or toggled.
  {
    const key: VaccineKey = 'HEPA'
    const anyRecorded = (grouped[key] ?? []).length > 0
    const include = Boolean(args.includeTravelOptional) || anyRecorded
    if (include) {
      const doses = countCompleted(grouped[key] ?? [])
      const last = mostRecentCompleted(grouped[key] ?? [])

      // Best-effort: 2-dose series, with a typical minimum interval ~6 months.
      const seriesTargetDoses = 2
      if (doses >= seriesTargetDoses) {
        out.push({
          key,
          status: 'up-to-date',
          title: 'Hepatitis A (travel/optional)',
          message:
            'Hepatitis A vaccination is typically travel/risk-based in Austria. At least two completed doses are recorded in this registry.',
          dosesRecorded: doses,
          lastDoseDate: last,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
          seriesTargetDoses,
        })
      } else if (doses === 1 && last) {
        const earliest = addMonthsIso(last, 6)
        const recommended = addMonthsIso(last, 12)

        const untilEarliest = earliest ? daysUntil(earliest, now) : null
        const untilRecommended = recommended ? daysUntil(recommended, now) : null

        const status: ReminderStatus = (() => {
          if (!recommended || untilRecommended === null) return 'unknown'
          // Due if past the ~12 month mark; due-soon if within/after minimum interval.
          if (untilRecommended <= 0) return 'due'
          if (earliest && untilEarliest !== null && untilEarliest <= 0) return 'due-soon'
          return 'up-to-date'
        })()

        const parts: string[] = []
        parts.push('Hepatitis A is typically travel/risk-based in Austria.')
        parts.push(`Dose 2 of ${seriesTargetDoses}.`)
        parts.push(`Last recorded dose: ${fmtDate(last)}.`)
        if (earliest) parts.push(`Earliest (min interval) date: ${fmtDate(earliest)}.`)
        if (recommended) parts.push(`Common target: ${fmtDate(recommended)} (best-effort).`)

        out.push({
          key,
          status,
          title: 'Hepatitis A (travel/optional)',
          message: parts.join(' '),
          dosesRecorded: doses,
          lastDoseDate: last,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
          nextDoseNumber: 2,
          seriesTargetDoses,
          earliestDueDate: earliest ?? undefined,
          nextDueDate: recommended ?? undefined,
          recommendedAgeLabel: 'Travel / risk-based',
        })
      } else {
        out.push({
          key,
          status: anyRecorded ? 'unknown' : 'unknown',
          title: 'Hepatitis A (travel/optional)',
          message:
            'Hepatitis A vaccination is typically travel/risk-based in Austria and not part of routine reminders. If travel or risk exposure applies, discuss vaccination with your clinician.',
          dosesRecorded: doses,
          scheduleProfile: profile,
          patientAgeYears: patientAge,
          seriesTargetDoses,
          recommendedAgeLabel: 'Travel / risk-based',
        })
      }
    }
  }

  // Sort: due first, then due-soon, then missing/unknown, then up-to-date.
  const rank: Record<ReminderStatus, number> = {
    due: 0,
    'due-soon': 1,
    missing: 2,
    unknown: 3,
    'up-to-date': 4,
  }
  return out.sort((a, b) => rank[a.status] - rank[b.status])
}
