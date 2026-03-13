import { getPractitionerBasicAuthHeader, getRelatedPersonBearerHeader } from '@/auth/auth'

const BASE = (import.meta.env.VITE_BACKEND_URL as string | undefined) ?? ''
const FHIR_BASE = ((import.meta.env.VITE_FHIR_URL as string | undefined) ?? '').replace(/\/$/, '')

function url(path: string) {
    if (!BASE) return path
        return `${BASE.replace(/\/$/, '')}${path.startsWith('/') ? '' : '/'}${path}`
}

function fhirUrl(path: string) {
    const normalized = path.startsWith('/') ? path : `/${path}`

    if (!FHIR_BASE) return `/fhir${normalized}`
        if (FHIR_BASE.endsWith('/fhir')) return `${FHIR_BASE}${normalized}`
            return `${FHIR_BASE}/fhir${normalized}`
}

export async function backendFetch(path: string, init: RequestInit = {}) {
    const headers = new Headers(init.headers ?? {})
    if (!headers.has('Accept')) headers.set('Accept', 'application/json,*/*')

        if (path.startsWith('/api/practitioner')) {
            const auth = getPractitionerBasicAuthHeader()
            if (auth) headers.set('Authorization', auth)
        } else if (path.startsWith('/api/related-person')) {
            const auth = getRelatedPersonBearerHeader()
            if (auth) headers.set('Authorization', auth)
        }

        const res = await fetch(url(path), { ...init, headers })

        if (!res.ok) {
            const text = await res.text().catch(() => '')
            throw new Error(`${res.status} ${res.statusText}${text ? `: ${text}` : ''}`)
        }

        return res
}

export async function fhirFetch(path: string, init: RequestInit = {}) {
    const headers = new Headers(init.headers ?? {})
    if (!headers.has('Accept')) headers.set('Accept', 'application/fhir+json,application/json,*/*')

        const res = await fetch(fhirUrl(path), { ...init, headers })

        if (!res.ok) {
            const text = await res.text().catch(() => '')
            throw new Error(`${res.status} ${res.statusText}${text ? `: ${text}` : ''}`)
        }

        return res
}
