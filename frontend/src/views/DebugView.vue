<script setup>
import { computed, onBeforeUnmount, onMounted, reactive, ref, watch } from 'vue'
import { backendFetch } from '@/api/backend'
import api from '@/services/api'
import { useAuth } from '@/auth/auth'

/**
 * Debug page goals:
 * - Run real backend requests through the same client code used by components (fetch + auth header behavior).
 * - Capture and display what the app is requesting while this page is open.
 * - (Dev builds) auto-discover request URLs used in the codebase to make it easy to pick an existing call.
 */

const { state, isAuthenticated } = useAuth()

// ------------------------
// Discovery (dev-only)
// ------------------------

function safeShortPath(p) {
  const idx = p.lastIndexOf('/src/')
  return idx >= 0 ? p.slice(idx + 5) : p
}

function normalizeApiPath(path) {
  if (!path) return path
  // If the call is written against axios baseURL ("/auth/login"), normalize to fetch/proxy form ("/api/auth/login").
  if (path.startsWith('http://') || path.startsWith('https://')) return path
  if (path.startsWith('/api/')) return path
  if (path.startsWith('/')) return `/api${path}`
  return `/api/${path}`
}

function stripQuotes(lit) {
  if (!lit || lit.length < 2) return lit
  const q = lit[0]
  if ((q === '"' || q === "'" || q === '`') && lit[lit.length - 1] === q) return lit.slice(1, -1)
  return lit
}

function discoverEndpoints() {
  if (!import.meta.env.DEV) return []

  // Load raw sources at build time (dev only). Vite will dead-code-eliminate this from prod builds.
  const modules = import.meta.glob(
    [
      '../components/**/*.vue',
      '../views/**/*.vue',
      '../services/**/*.{js,ts}',
      '../api/**/*.{js,ts}',
    ],
    { eager: true, import: 'default', query: '?raw' },
  )

  const items = []
  const seen = new Set()

  const reBackendFirstArg = /backendFetch\s*\(\s*(`[^`]*`|'[^']*'|"[^"]*")/g
  const reAxios = /\bapi\.(get|post|put|patch|delete)\s*\(\s*(`[^`]*`|'[^']*'|"[^"]*")/g

  for (const [file, raw] of Object.entries(modules)) {
    if (String(file).endsWith('/DebugView.vue')) continue
    const src = String(raw ?? '')
    if (!src) continue

    // backendFetch(...)
    for (const m of src.matchAll(reBackendFirstArg)) {
      const literal = m[1]
      const pathTemplate = stripQuotes(literal)
      const snippet = src.slice(m.index ?? 0, (m.index ?? 0) + 280)
      const mm = snippet.match(/\bmethod\s*:\s*['"]([A-Za-z]+)['"]/)
      const method = (mm?.[1] ?? 'GET').toUpperCase()
      const norm = `${method} ${pathTemplate}`
      if (seen.has(norm)) continue
      seen.add(norm)
      items.push({
        method,
        pathTemplate,
        path: pathTemplate,
        source: safeShortPath(file),
        kind: 'backendFetch',
      })
    }

    // axios api.get/post/... (services)
    for (const m of src.matchAll(reAxios)) {
      const method = String(m[1] ?? 'get').toUpperCase()
      const literal = m[2]
      const rawPath = stripQuotes(literal)
      const pathTemplate = normalizeApiPath(rawPath)
      const norm = `${method} ${pathTemplate}`
      if (seen.has(norm)) continue
      seen.add(norm)
      items.push({
        method,
        pathTemplate,
        path: pathTemplate,
        source: safeShortPath(file),
        kind: 'axios',
        rawPath,
      })
    }
  }

  // Sort: practitioner first, then auth, then others.
  items.sort((a, b) => {
    const pa = String(a.pathTemplate)
    const pb = String(b.pathTemplate)
    const ra = pa.includes('/api/practitioner') ? 0 : pa.includes('/api/auth') ? 1 : 2
    const rb = pb.includes('/api/practitioner') ? 0 : pb.includes('/api/auth') ? 1 : 2
    if (ra !== rb) return ra - rb
    if (a.method !== b.method) return a.method.localeCompare(b.method)
    return pa.localeCompare(pb)
  })

  return items
}

const discovered = discoverEndpoints()

// ------------------------
// Request builder
// ------------------------

function deriveVarKey(expr, index) {
  const cleaned = String(expr ?? '').replace(/\s+/g, ' ').trim()
  // Try to pick a stable identifier name.
  const lastIdent = cleaned.match(/([A-Za-z_][A-Za-z0-9_]*)\s*\)?\s*$/)
  const key = lastIdent?.[1] ?? `var${index + 1}`
  return key
}

function parseTemplateVars(pathTemplate) {
  const vars = []
  const re = /\$\{([^}]+)\}/g
  let idx = 0
  for (const m of String(pathTemplate ?? '').matchAll(re)) {
    const expr = m[1]
    vars.push({ expr, key: deriveVarKey(expr, idx++) })
  }
  // De-dupe keys if needed.
  const used = new Set()
  return vars.map((v) => {
    let k = v.key
    let n = 2
    while (used.has(k)) {
      k = `${v.key}${n++}`
    }
    used.add(k)
    return { ...v, key: k }
  })
}

function applyTemplateVars(pathTemplate, bindings, vars) {
  if (!vars?.length) return pathTemplate
  let i = 0
  return String(pathTemplate).replace(/\$\{([^}]+)\}/g, () => {
    const key = vars[i++]?.key
    const val = bindings?.[key]
    return val == null ? '' : String(val)
  })
}

const builder = reactive({
  method: 'GET',
  client: 'backendFetch', // backendFetch | rawFetch
  pathTemplate: '/api/auth/login',
  headersText: '{\n  "Accept": "application/json"\n}',
  bodyText: '',
  sendJson: true,
  templateVars: [],
  bindings: {},
})

const search = ref('')
const selectedId = ref('')

const filteredDiscovered = computed(() => {
  const q = search.value.trim().toLowerCase()
  if (!q) return discovered
  return discovered.filter((x) => `${x.method} ${x.pathTemplate} ${x.source}`.toLowerCase().includes(q))
})

function selectEndpoint(item) {
  selectedId.value = `${item.method} ${item.pathTemplate}`
  builder.method = item.method
  builder.pathTemplate = item.pathTemplate
  builder.templateVars = parseTemplateVars(builder.pathTemplate)
  // Preserve existing bindings when possible.
  const next = {}
  for (const v of builder.templateVars) next[v.key] = builder.bindings?.[v.key] ?? ''
  builder.bindings = next
  if (builder.method === 'GET' || builder.method === 'DELETE') builder.bodyText = ''
  if (!builder.headersText?.trim()) builder.headersText = '{\n  "Accept": "application/json"\n}'
}

watch(
  () => builder.pathTemplate,
  () => {
    builder.templateVars = parseTemplateVars(builder.pathTemplate)
    const next = {}
    for (const v of builder.templateVars) next[v.key] = builder.bindings?.[v.key] ?? ''
    builder.bindings = next
  },
)

// ------------------------
// History + capture
// ------------------------

const history = ref([])
const active = ref(null)
const running = ref(false)
const runError = ref('')
let seq = 1

function maskHeaders(h) {
  const out = {}
  try {
    for (const [k, v] of Object.entries(h ?? {})) {
      if (String(k).toLowerCase() === 'authorization') out[k] = '***'
      else out[k] = v
    }
  } catch {
    return h
  }
  return out
}

function pushHistory(entry) {
  history.value.unshift(entry)
  if (history.value.length > 200) history.value.pop()
}

async function readPreviewFromResponse(res) {
  try {
    const ct = res.headers?.get?.('content-type') ?? ''
    if (!/(application\/json|text\/|application\/problem\+json)/i.test(ct)) return ''
    const txt = await res.text()
    return txt.length > 4000 ? `${txt.slice(0, 4000)}\n…` : txt
  } catch {
    return ''
  }
}

async function send() {
  runError.value = ''
  active.value = null
  running.value = true

  const startedAt = performance.now()
  const startedIso = new Date().toISOString()
  const id = seq++

  let headers = {}
  try {
    headers = builder.headersText?.trim() ? JSON.parse(builder.headersText) : {}
  } catch (e) {
    running.value = false
    runError.value = `Invalid headers JSON: ${String(e)}`
    return
  }

  // Mark requests originating from this page, so the capture wrapper can label them.
  headers['X-Debug-Request'] = '1'

  let body = undefined
  if (!['GET', 'DELETE'].includes(builder.method)) {
    const raw = builder.bodyText ?? ''
    if (raw.trim()) {
      if (builder.sendJson) {
        try {
          const parsed = JSON.parse(raw)
          body = JSON.stringify(parsed)
          if (!Object.keys(headers).some((k) => k.toLowerCase() === 'content-type')) {
            headers['Content-Type'] = 'application/json'
          }
        } catch (e) {
          running.value = false
          runError.value = `Invalid body JSON: ${String(e)}`
          return
        }
      } else {
        body = raw
      }
    }
  }

  const templateVars = builder.templateVars
  const path = applyTemplateVars(builder.pathTemplate, builder.bindings, templateVars)
  if (String(path).includes('${')) {
    running.value = false
    runError.value = 'Unresolved template variables remain in the path.'
    return
  }

  try {
    let res
    if (builder.client === 'rawFetch') {
      res = await fetch(path, { method: builder.method, headers, body })
      if (!res.ok) {
        const t = await res.text().catch(() => '')
        throw new Error(`${res.status} ${res.statusText}${t ? `: ${t}` : ''}`)
      }
    } else {
      res = await backendFetch(path, { method: builder.method, headers, body })
    }

    const ct = res.headers?.get?.('content-type') ?? ''
    let parsed = null
    let text = ''
    if (/application\/json|application\/problem\+json/i.test(ct)) {
      parsed = await res.json().catch(() => null)
      if (parsed == null) text = await res.text().catch(() => '')
    } else {
      text = await res.text().catch(() => '')
    }

    const durationMs = Math.round(performance.now() - startedAt)
    const record = {
      id,
      startedIso,
      method: builder.method,
      url: path,
      source: 'debug',
      status: res.status,
      ok: !!res.ok,
      durationMs,
      request: { headers: maskHeaders(headers), body: body ?? '' },
      response: parsed != null ? parsed : text,
    }

    active.value = record
    pushHistory(record)
  } catch (e) {
    const durationMs = Math.round(performance.now() - startedAt)
    const record = {
      id,
      startedIso,
      method: builder.method,
      url: path,
      source: 'debug',
      status: null,
      ok: false,
      durationMs,
      request: { headers: maskHeaders(headers), body: body ?? '' },
      error: String(e),
    }
    active.value = record
    pushHistory(record)
    runError.value = String(e)
  } finally {
    running.value = false
  }
}

function clearHistory() {
  history.value = []
  active.value = null
  runError.value = ''
}

// Capture: fetch + axios (app traffic while this page is open).
let originalFetch = null
let axiosReqId = null
let axiosResId = null

function installCapture() {
  if (originalFetch) return

  // fetch()
  originalFetch = window.fetch.bind(window)
  window.fetch = async (input, init) => {
    const startedAt = performance.now()
    const startedIso = new Date().toISOString()
    const id = seq++

    const method = (init?.method || (input instanceof Request ? input.method : 'GET')).toUpperCase()
    const url = input instanceof Request ? input.url : String(input)
    const headers = init?.headers || (input instanceof Request ? Object.fromEntries(input.headers.entries()) : {})
    const isDebug =
      (typeof headers === 'object' && headers && String(headers['X-Debug-Request'] || headers['x-debug-request'] || '').trim() === '1') ||
      (input instanceof Request && input.headers?.get?.('X-Debug-Request') === '1')

    try {
      const res = await originalFetch(input, init)
      const durationMs = Math.round(performance.now() - startedAt)
      const preview = await readPreviewFromResponse(res.clone())

      // Requests created by this page are recorded with full detail by the request builder.
      if (!isDebug) {
        pushHistory({
          id,
          startedIso,
          method,
          url,
          source: 'app(fetch)',
          status: res.status,
          ok: res.ok,
          durationMs,
          responsePreview: preview,
        })
      }

      return res
    } catch (e) {
      const durationMs = Math.round(performance.now() - startedAt)
      if (!isDebug) {
        pushHistory({
          id,
          startedIso,
          method,
          url,
          source: 'app(fetch)',
          status: null,
          ok: false,
          durationMs,
          error: String(e),
        })
      }
      throw e
    }
  }

  // axios instance used by services (if any)
  axiosReqId = api.interceptors.request.use((config) => {
    config.headers = config.headers || {}
    config.__debugStartedAt = performance.now()
    config.__debugStartedIso = new Date().toISOString()
    return config
  })

  axiosResId = api.interceptors.response.use(
    async (resp) => {
      const startedAt = resp.config.__debugStartedAt ?? performance.now()
      const startedIso = resp.config.__debugStartedIso ?? new Date().toISOString()
      const durationMs = Math.round(performance.now() - startedAt)
      const id = seq++
      const method = String(resp.config.method ?? 'GET').toUpperCase()
      const url = String(resp.config.baseURL ?? '') + String(resp.config.url ?? '')
      const isDebug = String(resp.config.headers?.['X-Debug-Request'] ?? '') === '1'
      const preview = (() => {
        try {
          const data = resp.data
          const txt = typeof data === 'string' ? data : JSON.stringify(data, null, 2)
          return txt.length > 4000 ? `${txt.slice(0, 4000)}\n…` : txt
        } catch {
          return ''
        }
      })()
      pushHistory({
        id,
        startedIso,
        method,
        url,
        source: isDebug ? 'debug(axios)' : 'app(axios)',
        status: resp.status,
        ok: true,
        durationMs,
        responsePreview: preview,
      })
      return resp
    },
    async (err) => {
      const cfg = err?.config ?? {}
      const startedAt = cfg.__debugStartedAt ?? performance.now()
      const startedIso = cfg.__debugStartedIso ?? new Date().toISOString()
      const durationMs = Math.round(performance.now() - startedAt)
      const id = seq++
      const method = String(cfg.method ?? 'GET').toUpperCase()
      const url = String(cfg.baseURL ?? '') + String(cfg.url ?? '')
      const isDebug = String(cfg.headers?.['X-Debug-Request'] ?? '') === '1'
      pushHistory({
        id,
        startedIso,
        method,
        url,
        source: isDebug ? 'debug(axios)' : 'app(axios)',
        status: err?.response?.status ?? null,
        ok: false,
        durationMs,
        error: String(err),
      })
      return Promise.reject(err)
    },
  )
}

function uninstallCapture() {
  if (originalFetch) {
    window.fetch = originalFetch
    originalFetch = null
  }
  if (axiosReqId != null) {
    api.interceptors.request.eject(axiosReqId)
    axiosReqId = null
  }
  if (axiosResId != null) {
    api.interceptors.response.eject(axiosResId)
    axiosResId = null
  }
}

onMounted(() => {
  installCapture()
  if (discovered.length) selectEndpoint(discovered[0])
})

onBeforeUnmount(() => {
  uninstallCapture()
})

const effectiveUrl = computed(() => applyTemplateVars(builder.pathTemplate, builder.bindings, builder.templateVars))
const axiosBaseUrl = computed(() => String(api?.defaults?.baseURL ?? ''))
const backendBaseEnv = computed(() => String(import.meta.env.VITE_BACKEND_URL ?? ''))
const isDev = import.meta.env.DEV
</script>

<template>
  <div class="space-y-6">
    <div class="rounded-xl border border-gray-200 bg-white p-4">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <div class="text-xl font-semibold">Debug API</div>
          <div class="text-sm text-gray-600">
            Run backend requests with real auth headers, and see request history while you use the app.
          </div>
        </div>
        <div class="text-xs text-gray-600 text-right">
          <div>Auth: <span class="font-medium">{{ isAuthenticated ? 'signed in' : 'not signed in' }}</span></div>
          <div v-if="state?.practitioner">
            Doctor: <span class="font-medium">{{ state.practitioner.practitionerIdentifier }}</span>
          </div>
        </div>
      </div>

      <div class="mt-3 grid gap-2 text-xs text-gray-600">
        <div>
          <span class="font-medium">backendFetch:</span>
          <span v-if="backendBaseEnv">VITE_BACKEND_URL={{ backendBaseEnv }}</span>
          <span v-else>uses /api proxy (vite dev server) or relative /api in production</span>
        </div>
        <div>
          <span class="font-medium">axios baseURL:</span>
          <span>{{ axiosBaseUrl || '(not set)' }}</span>
        </div>
        <div class="text-orange-700" v-if="!isDev">
          Auto-discovery is disabled in production builds; use the request builder and the live history.
        </div>
      </div>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-2 gap-6">
      <!-- Discovered calls -->
      <div class="rounded-xl border border-gray-200 bg-white p-4">
        <div class="flex items-center justify-between gap-2">
          <div class="font-semibold">Discovered calls</div>
          <div class="text-xs text-gray-600">{{ filteredDiscovered.length }} shown</div>
        </div>

        <input
          v-model="search"
          class="mt-3 w-full rounded-md border border-gray-300 px-3 py-2 text-sm"
          placeholder="Filter (method, path, file)"
        />

        <div class="mt-3 max-h-[28rem] overflow-auto rounded-lg border border-gray-100">
          <div
            v-for="item in filteredDiscovered"
            :key="item.method + ' ' + item.pathTemplate + ' ' + item.source"
            class="cursor-pointer border-b border-gray-100 px-3 py-2 hover:bg-gray-50"
            :class="(item.method + ' ' + item.pathTemplate) === selectedId ? 'bg-blue-50' : ''"
            @click="selectEndpoint(item)"
          >
            <div class="flex items-center justify-between gap-2">
              <div class="text-sm">
                <span class="inline-flex w-14 justify-center rounded bg-gray-100 px-2 py-0.5 text-xs font-medium">{{ item.method }}</span>
                <span class="ml-2 font-mono text-xs">{{ item.pathTemplate }}</span>
              </div>
              <div class="text-[11px] text-gray-500">{{ item.kind }}</div>
            </div>
            <div class="mt-1 text-[11px] text-gray-500">{{ item.source }}</div>
          </div>

          <div v-if="!filteredDiscovered.length" class="p-3 text-sm text-gray-600">
            No entries. (If this is a production build, discovery is disabled.)
          </div>
        </div>
      </div>

      <!-- Builder -->
      <div class="rounded-xl border border-gray-200 bg-white p-4 space-y-4">
        <div class="font-semibold">Request builder</div>

        <div class="grid grid-cols-1 md:grid-cols-3 gap-3">
          <div>
            <label class="text-xs font-medium text-gray-700">Method</label>
            <select v-model="builder.method" class="mt-1 w-full rounded-md border border-gray-300 px-2 py-2 text-sm">
              <option>GET</option>
              <option>POST</option>
              <option>PUT</option>
              <option>PATCH</option>
              <option>DELETE</option>
            </select>
          </div>
          <div>
            <label class="text-xs font-medium text-gray-700">Client</label>
            <select v-model="builder.client" class="mt-1 w-full rounded-md border border-gray-300 px-2 py-2 text-sm">
              <option value="backendFetch">backendFetch (recommended)</option>
              <option value="rawFetch">raw fetch()</option>
            </select>
          </div>
          <div class="flex items-end">
            <button
              class="w-full rounded-md bg-blue-600 px-3 py-2 text-white disabled:opacity-60"
              :disabled="running"
              @click="send"
            >
              {{ running ? 'Sending…' : 'Send' }}
            </button>
          </div>
        </div>

        <div>
          <label class="text-xs font-medium text-gray-700">Path</label>
          <input
            v-model="builder.pathTemplate"
            class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm font-mono"
            placeholder="/api/..."
          />
          <div class="mt-1 text-[11px] text-gray-600">
            Effective URL: <span class="font-mono">{{ effectiveUrl }}</span>
          </div>
        </div>

        <div v-if="builder.templateVars.length" class="rounded-lg border border-gray-100 bg-gray-50 p-3">
          <div class="text-xs font-medium text-gray-700">Template variables</div>
          <div class="mt-2 grid grid-cols-1 md:grid-cols-2 gap-2">
            <div v-for="v in builder.templateVars" :key="v.key">
              <label class="text-[11px] text-gray-600">{{ v.key }}</label>
              <input
                v-model="builder.bindings[v.key]"
                class="mt-1 w-full rounded-md border border-gray-300 px-2 py-1.5 text-sm font-mono"
                :placeholder="v.expr"
              />
            </div>
          </div>
        </div>

        <div>
          <label class="text-xs font-medium text-gray-700">Headers (JSON)</label>
          <textarea
            v-model="builder.headersText"
            rows="6"
            class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm font-mono"
          />
          <div class="mt-2 flex items-center gap-2">
            <input id="sendJson" v-model="builder.sendJson" type="checkbox" class="h-4 w-4" />
            <label for="sendJson" class="text-sm text-gray-700">Treat body as JSON</label>
          </div>
        </div>

        <div>
          <label class="text-xs font-medium text-gray-700">Body</label>
          <textarea
            v-model="builder.bodyText"
            rows="8"
            class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2 text-sm font-mono"
            :placeholder="builder.method === 'GET' || builder.method === 'DELETE' ? '(no body)' : '{ }'"
          />
        </div>

        <p v-if="runError" class="text-sm text-red-600">{{ runError }}</p>

        <div v-if="active" class="rounded-lg border border-gray-100 bg-gray-50 p-3">
          <div class="flex items-center justify-between gap-2">
            <div class="text-sm font-medium">Latest response</div>
            <div class="text-xs text-gray-600">{{ active.durationMs }} ms</div>
          </div>
          <div class="mt-1 text-xs text-gray-700">
            <span class="font-mono">{{ active.method }} {{ active.url }}</span>
            <span v-if="active.status != null" class="ml-2">Status: <span class="font-medium">{{ active.status }}</span></span>
          </div>
          <pre class="mt-2 overflow-auto rounded-md bg-white p-2 text-xs">{{ active.error ? active.error : JSON.stringify(active.response, null, 2) }}</pre>
        </div>
      </div>
    </div>

    <!-- History -->
    <div class="rounded-xl border border-gray-200 bg-white p-4">
      <div class="flex items-center justify-between gap-2">
        <div class="font-semibold">Request history (live capture)</div>
        <button class="rounded-md border border-gray-300 px-3 py-1.5 text-sm" @click="clearHistory">Clear</button>
      </div>
      <div class="mt-3 overflow-auto rounded-lg border border-gray-100">
        <table class="min-w-full text-sm">
          <thead class="bg-gray-50 text-xs text-gray-600">
            <tr>
              <th class="px-3 py-2 text-left">Time</th>
              <th class="px-3 py-2 text-left">Source</th>
              <th class="px-3 py-2 text-left">Method</th>
              <th class="px-3 py-2 text-left">URL</th>
              <th class="px-3 py-2 text-left">Status</th>
              <th class="px-3 py-2 text-left">ms</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="h in history" :key="h.id" class="border-t border-gray-100 align-top">
              <td class="px-3 py-2 font-mono text-xs text-gray-600">{{ (h.startedIso || '').slice(11, 19) }}</td>
              <td class="px-3 py-2 text-xs text-gray-600">{{ h.source }}</td>
              <td class="px-3 py-2">
                <span class="inline-flex w-14 justify-center rounded bg-gray-100 px-2 py-0.5 text-xs font-medium">{{ h.method }}</span>
              </td>
              <td class="px-3 py-2 font-mono text-xs">
                <div class="max-w-[42rem] break-all">{{ h.url }}</div>
                <details v-if="h.responsePreview || h.error" class="mt-1">
                  <summary class="cursor-pointer text-xs text-gray-600">details</summary>
                  <pre class="mt-2 overflow-auto rounded-md bg-gray-50 p-2 text-xs">{{ h.error ? h.error : h.responsePreview }}</pre>
                </details>
              </td>
              <td class="px-3 py-2 text-xs">
                <span v-if="h.status != null" :class="h.ok ? 'text-green-700' : 'text-red-700'">{{ h.status }}</span>
                <span v-else class="text-red-700">error</span>
              </td>
              <td class="px-3 py-2 text-xs text-gray-600">{{ h.durationMs }}</td>
            </tr>

            <tr v-if="!history.length">
              <td colspan="6" class="px-3 py-6 text-center text-sm text-gray-600">No requests captured yet.</td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>
