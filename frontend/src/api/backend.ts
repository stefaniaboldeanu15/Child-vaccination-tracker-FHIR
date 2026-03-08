// src/api/backend.ts
const BASE = (import.meta.env.VITE_BACKEND_URL as string | undefined) ?? 'http://localhost:8081'

function url(path: string) {
    const normalizedBase = BASE.replace(/\/$/, '')
    const normalizedPath = path.startsWith('/') ? path : `/${path}`
    return `${normalizedBase}${normalizedPath}`
}

export async function backendFetch(path: string, init: RequestInit = {}) {
    const headers = new Headers(init.headers ?? {})

    if (!headers.has('Accept')) {
        headers.set('Accept', 'application/json,*/*')
    }

    const res = await fetch(url(path), { ...init, headers })

    if (!res.ok) {
        const text = await res.text().catch(() => '')
        throw new Error(`${res.status} ${res.statusText}${text ? `: ${text}` : ''}`)
    }

    return res
}