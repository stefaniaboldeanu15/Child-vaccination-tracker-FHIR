import { getAccessToken } from '@/auth/session'

const BACKEND_BASE = (import.meta.env.VITE_BACKEND_URL as string | undefined) ?? 'http://localhost:8081'
const FHIR_BASE = ((import.meta.env.VITE_FHIR_URL as string | undefined) ?? '').replace(/\/fhir\/?$/, '')

function absolute(base: string, path: string) {
  if (/^https?:\/\//.test(path)) return path
  return `${base.replace(/\/$/, '')}${path.startsWith('/') ? '' : '/'}${path}`
}

export function backendUrl(path: string) {
  return absolute(BACKEND_BASE, path)
}

export function fhirUrl(path: string) {
  if (!FHIR_BASE) {
    return path.startsWith('/fhir') ? path : `/fhir${path.startsWith('/') ? '' : '/'}${path}`
  }

  const normalizedPath = path.startsWith('/fhir') ? path.slice(5) || '/' : path
  return absolute(`${FHIR_BASE}/fhir`, normalizedPath)
}

export async function backendFetch(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers ?? {})
  if (!headers.has('Accept')) headers.set('Accept', 'application/json')
  const token = getAccessToken()
  if (token) headers.set('Authorization', `Bearer ${token}`)
  const response = await fetch(backendUrl(path), { ...init, headers })

  if (!response.ok) {
    const text = await response.text().catch(() => '')
    throw new Error(`${response.status} ${response.statusText}${text ? `: ${text}` : ''}`)
  }

  return response
}

export async function backendJson<T>(path: string, init: RequestInit = {}) {
  const response = await backendFetch(path, init)
  return (await response.json()) as T
}

export async function fhirFetch(path: string, init: RequestInit = {}) {
  const headers = new Headers(init.headers ?? {})
  if (!headers.has('Accept')) headers.set('Accept', 'application/fhir+json,application/json')
  const response = await fetch(fhirUrl(path), { ...init, headers })

  if (!response.ok) {
    const text = await response.text().catch(() => '')
    throw new Error(`${response.status} ${response.statusText}${text ? `: ${text}` : ''}`)
  }

  return response
}

export async function fhirJson<T>(path: string, init: RequestInit = {}) {
  const response = await fhirFetch(path, init)
  return (await response.json()) as T
}
