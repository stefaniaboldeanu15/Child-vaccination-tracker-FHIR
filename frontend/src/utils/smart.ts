import { backendJson, backendUrl } from '@/api/http'
import { useSession, type SessionRole } from '@/auth/session'

type SmartConfiguration = {
  authorization_endpoint: string
  token_endpoint: string
}

type TokenResponse = {
  access_token: string
}

type PendingLaunch = {
  state: string
  verifier: string
  role: SessionRole
  tokenEndpoint: string
}

const PENDING_KEY = 'vaxcare-smart-pending-v1'
const CLIENT_ID = (import.meta.env.VITE_SMART_CLIENT_ID as string | undefined) ?? 'child-vax-ui'
const REDIRECT_PATH = '/auth/callback'

function redirectUri() {
  return `${window.location.origin}${REDIRECT_PATH}`
}

function scopeForRole(role: SessionRole) {
  return role === 'practitioner'
    ? 'openid profile launch user/*.read user/*.write patient/*.read patient/*.write'
    : 'openid profile launch/patient patient/*.read patient/*.write user/*.read user/*.write'
}

function storePendingLaunch(pending: PendingLaunch) {
  sessionStorage.setItem(PENDING_KEY, JSON.stringify(pending))
}

function loadPendingLaunch(): PendingLaunch | null {
  try {
    const raw = sessionStorage.getItem(PENDING_KEY)
    if (!raw) return null
    return JSON.parse(raw) as PendingLaunch
  } catch {
    return null
  }
}

function clearPendingLaunch() {
  sessionStorage.removeItem(PENDING_KEY)
}

function randomString(length = 64) {
  const bytes = new Uint8Array(length)
  crypto.getRandomValues(bytes)
  return Array.from(bytes, (byte) => byte.toString(16).padStart(2, '0')).join('').slice(0, length)
}

async function sha256Base64Url(value: string) {
  const data = new TextEncoder().encode(value)
  const hash = await crypto.subtle.digest('SHA-256', data)
  const bytes = new Uint8Array(hash)
  let binary = ''
  bytes.forEach((byte) => {
    binary += String.fromCharCode(byte)
  })
  return btoa(binary).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/, '')
}

export async function startSmartLogin(role: SessionRole) {
  const discovery = await backendJson<SmartConfiguration>('/.well-known/smart-configuration')
  const state = randomString(32)
  const verifier = randomString(64)
  const challenge = await sha256Base64Url(verifier)

  storePendingLaunch({
    state,
    verifier,
    role,
    tokenEndpoint: discovery.token_endpoint,
  })

  const url = new URL(discovery.authorization_endpoint)
  url.searchParams.set('response_type', 'code')
  url.searchParams.set('client_id', CLIENT_ID)
  url.searchParams.set('redirect_uri', redirectUri())
  url.searchParams.set('scope', scopeForRole(role))
  url.searchParams.set('state', state)
  url.searchParams.set('code_challenge', challenge)
  url.searchParams.set('code_challenge_method', 'S256')
  url.searchParams.set('role', role)

  window.location.assign(url.toString())
}

export async function finishSmartLogin(search: URLSearchParams) {
  const code = search.get('code')
  const state = search.get('state')
  const pending = loadPendingLaunch()

  if (!code || !state || !pending || pending.state !== state) {
    throw new Error('SMART launch state is invalid or missing.')
  }

  const body = new URLSearchParams({
    grant_type: 'authorization_code',
    code,
    redirect_uri: redirectUri(),
    client_id: CLIENT_ID,
    code_verifier: pending.verifier,
  })

  const tokenResponse = await fetch(pending.tokenEndpoint, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body,
  })

  if (!tokenResponse.ok) {
    const text = await tokenResponse.text().catch(() => '')
    throw new Error(`Token exchange failed${text ? `: ${text}` : ''}`)
  }

  const token = (await tokenResponse.json()) as TokenResponse
  const { applyToken, applyProfile } = useSession()
  applyToken(token.access_token)
  const profileResponse = await fetch(backendUrl('/api/auth/me'), {
    headers: { Authorization: `Bearer ${token.access_token}` },
  })
  if (!profileResponse.ok) {
    throw new Error('The session profile could not be loaded.')
  }

  applyProfile(await profileResponse.json())
  clearPendingLaunch()
  return pending.role
}
