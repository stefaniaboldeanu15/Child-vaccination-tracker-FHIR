import { backendJson } from '@/api/http'
import type { SessionRole } from '@/auth/session'

export type AuthSessionProfile = {
  username: string
  role: SessionRole
  displayName: string
  fhirUser: string
  practitionerId?: string | null
  relatedPersonIds?: string[]
  patientIds?: string[]
}

export type PractitionerRegistrationRequest = {
  username: string
  password: string
  identifier?: string
  firstName?: string
  lastName?: string
  specialization?: string
  phone?: string
  email?: string
  organizationId?: string
}

export type RelatedPersonRegistrationRequest = {
  username: string
  password: string
  patientId?: string
  firstName?: string
  lastName?: string
  relationship?: string
  phone?: string
  email?: string
  address?: string
}

export function registerPractitioner(payload: PractitionerRegistrationRequest) {
  return backendJson<AuthSessionProfile>('/api/auth/practitioner/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
}

export function registerRelatedPerson(payload: RelatedPersonRegistrationRequest) {
  return backendJson<AuthSessionProfile>('/api/auth/related-person/register', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })
}
