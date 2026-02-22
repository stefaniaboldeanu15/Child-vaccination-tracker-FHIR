import { computed, reactive } from 'vue'

export type Practitioner = {
    practitionerId: string
    practitionerIdentifier: string
    firstName?: string
    lastName?: string
}

type AuthState = {
    role: 'doctor' | null
    identifier: string | null
    password: string | null
    practitioner: Practitioner | null
}

const STORAGE_KEY = 'vax_registry_auth_v2'

function load(): AuthState {
    try {
        const raw = localStorage.getItem(STORAGE_KEY)
        if (!raw) return { role: null, identifier: null, password: null, practitioner: null }
        const p = JSON.parse(raw) as Partial<AuthState>
        return {
            role: p.role === 'doctor' ? 'doctor' : null,
            identifier: typeof p.identifier === 'string' ? p.identifier : null,
            password: typeof p.password === 'string' ? p.password : null,
            practitioner: p.practitioner ?? null,
        }
    } catch {
        return { role: null, identifier: null, password: null, practitioner: null }
    }
}

function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

const state = reactive<AuthState>(load())

export function useAuth() {
    const isAuthenticated = computed(() => state.role === 'doctor' && !!state.identifier && !!state.password)

    function loginAsDoctor(identifier: string, password: string, practitioner: Practitioner) {
        state.role = 'doctor'
        state.identifier = identifier.trim()
        state.password = password
        state.practitioner = practitioner
        persist()
    }

    function logout() {
        state.role = null
        state.identifier = null
        state.password = null
        state.practitioner = null
        persist()
    }

    return { state, isAuthenticated, loginAsDoctor, logout }
}

export function getPractitionerBasicAuthHeader(): string | null {
    if (!state.identifier || !state.password) return null
        return `Basic ${btoa(`${state.identifier}:${state.password}`)}`
}
