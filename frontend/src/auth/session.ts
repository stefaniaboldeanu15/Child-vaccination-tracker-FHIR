import { computed, reactive } from 'vue'

export type SessionRole = 'practitioner' | 'related-person'

export type SessionState = {
  accessToken: string | null
  username: string | null
  role: SessionRole | null
  displayName: string | null
  fhirUser: string | null
  practitionerId: string | null
  relatedPersonIds: string[]
  patientIds: string[]
}

type ApiSessionResponse = {
  username: string
  role: SessionRole
  displayName: string
  fhirUser: string
  practitionerId?: string | null
  relatedPersonIds?: string[]
  patientIds?: string[]
}

const STORAGE_KEY = 'vaxcare-smart-session-v1'

function emptySession(): SessionState {
  return {
    accessToken: null,
    username: null,
    role: null,
    displayName: null,
    fhirUser: null,
    practitionerId: null,
    relatedPersonIds: [],
    patientIds: [],
  }
}

function load(): SessionState {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return emptySession()
    const parsed = JSON.parse(raw) as Partial<SessionState>

    return {
      accessToken: typeof parsed.accessToken === 'string' ? parsed.accessToken : null,
      username: typeof parsed.username === 'string' ? parsed.username : null,
      role: parsed.role === 'practitioner' || parsed.role === 'related-person' ? parsed.role : null,
      displayName: typeof parsed.displayName === 'string' ? parsed.displayName : null,
      fhirUser: typeof parsed.fhirUser === 'string' ? parsed.fhirUser : null,
      practitionerId: typeof parsed.practitionerId === 'string' ? parsed.practitionerId : null,
      relatedPersonIds: Array.isArray(parsed.relatedPersonIds) ? parsed.relatedPersonIds.filter(Boolean) : [],
      patientIds: Array.isArray(parsed.patientIds) ? parsed.patientIds.filter(Boolean) : [],
    }
  } catch {
    return emptySession()
  }
}

const state = reactive<SessionState>(load())

function persist() {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

export function useSession() {
  const isAuthenticated = computed(() => !!state.accessToken && !!state.role)

  function applyToken(accessToken: string) {
    state.accessToken = accessToken
    persist()
  }

  function applyProfile(profile: ApiSessionResponse) {
    state.username = profile.username
    state.role = profile.role
    state.displayName = profile.displayName
    state.fhirUser = profile.fhirUser
    state.practitionerId = profile.practitionerId ?? null
    state.relatedPersonIds = profile.relatedPersonIds ?? []
    state.patientIds = profile.patientIds ?? []
    persist()
  }

  function clear() {
    Object.assign(state, emptySession())
    localStorage.removeItem(STORAGE_KEY)
  }

  return { state, isAuthenticated, applyToken, applyProfile, clear }
}

export function getAccessToken() {
  return state.accessToken
}
