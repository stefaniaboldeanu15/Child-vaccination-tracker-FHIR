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

type RelatedPersonAuthState = {
    token: string | null
    patientId: string | null
    firstName: string | null
    lastName: string | null
}

const STORAGE_KEY = 'vax_registry_auth_v2'
const RP_STORAGE_KEY = 'vax_registry_rp_auth_v1'

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

function loadRelatedPerson(): RelatedPersonAuthState {
    try {
        const raw = localStorage.getItem(RP_STORAGE_KEY)
        if (!raw) return { token: null, patientId: null, firstName: null, lastName: null }
        const p = JSON.parse(raw) as Partial<RelatedPersonAuthState>
        return {
            token: typeof p.token === 'string' ? p.token : null,
            patientId: typeof p.patientId === 'string' ? p.patientId : null,
            firstName: typeof p.firstName === 'string' ? p.firstName : null,
            lastName: typeof p.lastName === 'string' ? p.lastName : null,
        }
    } catch {
        return { token: null, patientId: null, firstName: null, lastName: null }
    }
}

function persist() {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(state))
}

function persistRelatedPerson() {
    localStorage.setItem(RP_STORAGE_KEY, JSON.stringify(rpState))
}

const state = reactive<AuthState>(load())
const rpState = reactive<RelatedPersonAuthState>(loadRelatedPerson())

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

export function useRelatedPersonAuth() {
    const isAuthenticated = computed(() => !!rpState.token && !!rpState.patientId)

    function login(token: string, patientId: string, firstName: string | null, lastName: string | null) {
        rpState.token = token
        rpState.patientId = patientId
        rpState.firstName = firstName
        rpState.lastName = lastName
        persistRelatedPerson()
    }

    function logout() {
        rpState.token = null
        rpState.patientId = null
        rpState.firstName = null
        rpState.lastName = null
        persistRelatedPerson()
    }

    return { rpState, isAuthenticated, login, logout }
}

export function getPractitionerBasicAuthHeader(): string | null {
    if (!state.identifier || !state.password) return null
        return `Basic ${btoa(`${state.identifier}:${state.password}`)}`
}

export function getRelatedPersonBearerHeader(): string | null {
    if (!rpState.token) return null
        return `Bearer ${rpState.token}`
}
