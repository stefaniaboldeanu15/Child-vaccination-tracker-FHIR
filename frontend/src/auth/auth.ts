import { computed, reactive } from 'vue'

export type UserRole = 'doctor' | 'patient' | 'guardian'

type AuthState = {
  role: UserRole | null
  patientId: string | null
  doctorId: string | null
  guardianId: string | null
}

const STORAGE_KEY = 'vax_registry_auth_v1'

function load(): AuthState {
  try {
    const raw = window.localStorage.getItem(STORAGE_KEY)
    if (!raw) return { role: null, patientId: null, doctorId: null, guardianId: null }
    const parsed = JSON.parse(raw) as Partial<AuthState>
    return {
      role: parsed.role === 'doctor' || parsed.role === 'patient' || parsed.role === 'guardian' ? parsed.role : null,
      patientId: typeof parsed.patientId === 'string' ? parsed.patientId : null,
      doctorId: typeof parsed.doctorId === 'string' ? parsed.doctorId : null,
      guardianId: typeof parsed.guardianId === 'string' ? parsed.guardianId : null,
    }
  } catch {
    return { role: null, patientId: null, doctorId: null, guardianId: null }
  }
}

function persist(state: AuthState) {
  window.localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

const state = reactive<AuthState>(load())

export function useAuth() {
  const isAuthenticated = computed(() => state.role !== null)

  function loginAsPatient(patientId: string) {
    state.role = 'patient'
    state.patientId = patientId.trim()
    state.doctorId = null
    state.guardianId = null
    persist(state)
  }

  function loginAsDoctor(doctorId: string) {
    state.role = 'doctor'
    state.doctorId = doctorId.trim() || 'doctor'
    state.patientId = null
    state.guardianId = null
    persist(state)
  }

  function loginAsGuardian(guardianId: string) {
    state.role = 'guardian'
    state.guardianId = guardianId.trim()
    state.patientId = null
    state.doctorId = null
    persist(state)
  }

  function logout() {
    state.role = null
    state.patientId = null
    state.doctorId = null
    state.guardianId = null
    persist(state)
  }

  return {
    state,
    isAuthenticated,
    loginAsPatient,
    loginAsDoctor,
    loginAsGuardian,
    logout,
  }
}
