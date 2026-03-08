// src/auth/auth.ts
import { computed, reactive } from 'vue'

export type Practitioner = {
    practitionerId: string
    practitionerIdentifier: string
    firstName?: string
    lastName?: string
}

type AppState = {
    practitioner: Practitioner | null
}

const state = reactive<AppState>({
    practitioner: {
        practitionerId: import.meta.env.VITE_PRACTITIONER_ID ?? '',
        practitionerIdentifier: import.meta.env.VITE_PRACTITIONER_IDENTIFIER ?? '',
        firstName: import.meta.env.VITE_PRACTITIONER_FIRST_NAME ?? '',
        lastName: import.meta.env.VITE_PRACTITIONER_LAST_NAME ?? '',
    },
})

export function useAuth() {
    const isAuthenticated = computed(() => true)

    function logout() {
        state.practitioner = {
            practitionerId: import.meta.env.VITE_PRACTITIONER_ID ?? '',
            practitionerIdentifier: import.meta.env.VITE_PRACTITIONER_IDENTIFIER ?? '',
            firstName: import.meta.env.VITE_PRACTITIONER_FIRST_NAME ?? '',
            lastName: import.meta.env.VITE_PRACTITIONER_LAST_NAME ?? '',
        }
    }

    return {
        state,
        isAuthenticated,
        loginAsDoctor: () => {},
        logout,
    }
}

export function getPractitionerBasicAuthHeader(): string | null {
    return null
}