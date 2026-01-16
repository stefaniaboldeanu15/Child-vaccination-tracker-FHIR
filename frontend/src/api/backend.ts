const BASE = (import.meta.env.VITE_BACKEND_URL as string | undefined) ?? ''

function url(path: string) {
  if (!BASE) return path
  return `${BASE.replace(/\/$/, '')}${path.startsWith('/') ? '' : '/'}${path}`
}

export async function backendFetch(path: string, init: RequestInit = {}) {
  const res = await fetch(url(path), {
    ...init,
    headers: {
      ...(init.headers ?? {}),
      Accept: 'application/fhir+json,application/json,*/*',
    },
  })

  if (!res.ok) {
    const text = await res.text().catch(() => '')
    throw new Error(`${res.status} ${res.statusText}${text ? `: ${text}` : ''}`)
  }
  return res
}

export async function downloadPdf(path: string, filename: string) {
  const res = await backendFetch(path, { headers: { Accept: 'application/pdf' } })
  const blob = await res.blob()
  const u = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = u
  a.download = filename
  document.body.appendChild(a)
  a.click()
  a.remove()
  URL.revokeObjectURL(u)
}

export type IceRecommendation = {
  focusCode?: string | null
  focusDisplayName?: string | null
  recommendationCode?: string | null
  recommendationDisplay?: string | null
  reasonCode?: string | null
  reasonDisplay?: string | null
  earliestDate?: string | null
  recommendedDate?: string | null
  pastDueDate?: string | null
}

export type IceForecastResponse = {
  service: 'ICE'
  endpointUrl: string
  km: { scopingEntityId: string; businessId: string; version: string }
  patientId: string
  specifiedTime: string
  inputDosesIncluded: number
  recommendations: IceRecommendation[]
}

export async function getIceForecast(patientId: string, specifiedTime?: string): Promise<IceForecastResponse> {
  const qs = specifiedTime ? `?specifiedTime=${encodeURIComponent(specifiedTime)}` : ''
  const res = await backendFetch(`/api/cds/ice/forecast/${encodeURIComponent(patientId)}${qs}`)
  return (await res.json()) as IceForecastResponse
}
