import { getPractitionerBasicAuthHeader, getRelatedPersonBearerHeader } from '@/auth/auth'

const BASE = (import.meta.env.VITE_BACKEND_URL as string | undefined) ?? ''

function url(path: string) {
    if (!BASE) return path
        return `${BASE.replace(/\/$/, '')}${path.startsWith('/') ? '' : '/'}${path}`
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
