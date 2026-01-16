<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { backendFetch } from '@/api/backend'
import { useAuth } from '@/auth/auth'

const DEBUG_KEY = 'prt-debug-register'

const router = useRouter()
const { loginAsPatient, loginAsDoctor, loginAsGuardian } = useAuth()

const mode = ref<'patient' | 'doctor' | 'guardian'>('guardian')

// All portals authenticate against the backend (which queries the configured FHIR server)
const identifier = ref('')
const password = ref('')

const error = ref<string | null>(null)
const loading = ref(false)

// Debug-only register UI: hidden behind a click-unlock on the header.
const debugClicks = ref(0)
const debugDialogOpen = ref(false)
const debugUnlocked = ref<boolean>(localStorage.getItem(DEBUG_KEY) === 'true')
const debugEnabled = computed(() => debugUnlocked.value)

let debugTimer: number | undefined
function onHeaderClick() {
  // 7 clicks within 2 seconds toggles the debug register UI.
  debugClicks.value += 1
  if (debugTimer) window.clearTimeout(debugTimer)
  debugTimer = window.setTimeout(() => (debugClicks.value = 0), 2000)
  if (debugClicks.value >= 7) {
    debugUnlocked.value = !debugUnlocked.value
    localStorage.setItem(DEBUG_KEY, String(debugUnlocked.value))
    debugClicks.value = 0
  }
}

type RegisterMode = 'doctor' | 'patient' | 'guardian'
const registerMode = ref<RegisterMode>('doctor')

// Doctor registration (Practitioner)
const regDoctorIdentifier = ref('')
const regDoctorPassword = ref('')
const regDoctorFirstName = ref('')
const regDoctorLastName = ref('')

// Patient registration
const regPatientIdentifier = ref('')
const regPatientPassword = ref('')
const regPatientFirstName = ref('')
const regPatientLastName = ref('')
const regPatientBirthDate = ref('')
const regPatientGender = ref<'male' | 'female' | 'other' | 'unknown'>('unknown')

// Guardian registration (RelatedPerson) + Consent
const regGuardianPatientId = ref('')
const regGuardianIdentifier = ref('')
const regGuardianPassword = ref('')
const regGuardianFirstName = ref('')
const regGuardianLastName = ref('')
const regGuardianRelationship = ref('parent')

const regResult = ref<string | null>(null)
const regError = ref<string | null>(null)
const regLoading = ref(false)

function setLoginFieldsFromRegister(id: string, pw: string) {
  identifier.value = id
  password.value = pw
}

async function submitRegister() {
  regResult.value = null
  regError.value = null
  regLoading.value = true
  try {
    if (registerMode.value === 'doctor') {
      const id = regDoctorIdentifier.value.trim()
      if (!id || !regDoctorPassword.value) throw new Error('Identifier and password are required.')

      await backendFetch('/api/auth/register/practitioner', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          identifierValue: id,
          identifierSystem: undefined,
          password: regDoctorPassword.value,
          givenName: regDoctorFirstName.value || undefined,
          familyName: regDoctorLastName.value || undefined,
        }),
      })

      regResult.value = `Doctor registered with identifier: ${id}`
      mode.value = 'doctor'
      setLoginFieldsFromRegister(id, regDoctorPassword.value)
      return
    }

    if (registerMode.value === 'patient') {
      const id = regPatientIdentifier.value.trim()
      if (!id || !regPatientPassword.value) throw new Error('Identifier and password are required.')

      const res = await backendFetch('/api/physician/patients', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          identifierValue: id,
          identifierSystem: undefined,
          password: regPatientPassword.value,
          givenName: regPatientFirstName.value || undefined,
          familyName: regPatientLastName.value || undefined,
          birthDate: regPatientBirthDate.value || undefined,
          gender: regPatientGender.value || undefined,
        }),
      })
      const json = (await res.json()) as { patientId?: string }
      regResult.value = `Patient created (FHIR id: ${json.patientId ?? 'unknown'}). Login uses identifier: ${id}`
      mode.value = 'patient'
      setLoginFieldsFromRegister(id, regPatientPassword.value)
      return
    }

    // guardian
    const patientId = regGuardianPatientId.value.trim()
    if (!patientId) throw new Error('Patient FHIR id is required (e.g. "12345").')
    const gid = regGuardianIdentifier.value.trim()
    if (!gid || !regGuardianPassword.value) throw new Error('Guardian identifier and password are required.')

    const res1 = await backendFetch(`/api/physician/patients/${encodeURIComponent(patientId)}/guardians`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        identifierValue: gid,
        identifierSystem: undefined,
        password: regGuardianPassword.value,
        givenName: regGuardianFirstName.value || undefined,
        familyName: regGuardianLastName.value || undefined,
        relationshipText: regGuardianRelationship.value || undefined,
      }),
    })
    const created = (await res1.json()) as { guardianId?: string }
    const guardianId = created.guardianId
    if (!guardianId) throw new Error('Guardian creation failed (missing id).')

    const res2 = await backendFetch(
      `/api/physician/patients/${encodeURIComponent(patientId)}/guardians/${encodeURIComponent(guardianId)}/consents`,
      { method: 'POST' },
    )
    const consent = (await res2.json()) as { consentId?: string }

    regResult.value = `Guardian created (FHIR id: ${guardianId}) and consent granted (Consent id: ${consent.consentId ?? 'unknown'}). Login uses identifier: ${gid}`
    mode.value = 'guardian'
    setLoginFieldsFromRegister(gid, regGuardianPassword.value)
  } catch (e) {
    regError.value = String(e)
  } finally {
    regLoading.value = false
  }
}

async function submit() {
  error.value = null

  const id = identifier.value.trim()
  if (!id) {
    error.value = 'Identifier is required.'
    return
  }

  loading.value = true
  try {
    const res = await backendFetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ identifier: id, password: password.value }),
    })

    const json = await res.json() as { role?: string; guardianId?: string; patientId?: string; doctorId?: string }

    if (json.role === 'guardian' && json.guardianId) {
      loginAsGuardian(json.guardianId)
      router.push('/guardian')
      return
    }

    if (json.role === 'patient' && json.patientId) {
      loginAsPatient(json.patientId)
      router.push('/patient')
      return
    }

    if (json.role === 'doctor') {
      loginAsDoctor(json.doctorId ?? 'doctor')
      router.push('/doctor')
      return
    }

    throw new Error('Unexpected login response')
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="max-w-xl mx-auto space-y-6">
    <div class="rounded-xl border border-gray-200 bg-white p-4" @click="onHeaderClick">
      <div class="text-xl font-semibold">Sign in</div>
      <div class="text-sm text-gray-600">Select portal and continue.</div>
    </div>

    <div class="rounded-xl border border-gray-200 bg-white p-4 space-y-4">
      <div class="flex gap-2">
        <button class="flex-1 rounded-md border px-3 py-2" :class="mode==='guardian' ? 'bg-blue-600 text-white' : 'bg-white'" @click="mode='guardian'">Guardian</button>
        <button class="flex-1 rounded-md border px-3 py-2" :class="mode==='patient' ? 'bg-blue-600 text-white' : 'bg-white'" @click="mode='patient'">Patient</button>
        <button class="flex-1 rounded-md border px-3 py-2" :class="mode==='doctor' ? 'bg-blue-600 text-white' : 'bg-white'" @click="mode='doctor'">Doctor</button>
      </div>

      <div class="space-y-3">
        <div>
          <label class="text-sm font-medium text-gray-700">
            {{ mode === 'doctor' ? 'Doctor identifier' : mode === 'patient' ? 'Patient identifier' : 'Guardian identifier' }}
          </label>
          <input v-model="identifier" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="e.g., identifier" />
        </div>
        <div>
          <label class="text-sm font-medium text-gray-700">Password</label>
          <input v-model="password" type="password" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="password" />
        </div>
      </div>

      <div v-if="error" class="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-700">{{ error }}</div>

      <button class="w-full rounded-md bg-blue-600 px-3 py-2 text-white hover:bg-blue-700 disabled:opacity-50" :disabled="loading" @click="submit">
        Continue
      </button>
    </div>

    <div v-if="debugEnabled" class="rounded-xl border border-gray-200 bg-white p-4">
      <div class="flex items-center justify-between">
        <div>
          <div class="text-sm font-medium">Debug tools</div>
          <div class="text-xs text-gray-600">Hidden register UI for local testing. Stored on a public FHIR server.</div>
        </div>
        <button class="rounded-md border px-3 py-2 text-sm" @click="debugDialogOpen = true">Register…</button>
      </div>

      <div class="mt-2 text-xs text-gray-500">Tip: click the header 7 times to toggle this section.</div>
    </div>

    <div v-if="debugDialogOpen" class="fixed inset-0 z-50 flex items-center justify-center bg-black/40">
      <div class="w-full max-w-xl rounded-xl bg-white p-4 shadow">
        <div class="flex items-center justify-between">
          <div class="text-lg font-semibold">Debug register</div>
          <button class="rounded-md border px-3 py-1 text-sm" @click="debugDialogOpen = false">Close</button>
        </div>

        <div class="mt-3 flex gap-2">
          <button class="flex-1 rounded-md border px-3 py-2 text-sm" :class="registerMode==='doctor' ? 'bg-blue-600 text-white' : 'bg-white'" @click="registerMode='doctor'">Doctor</button>
          <button class="flex-1 rounded-md border px-3 py-2 text-sm" :class="registerMode==='patient' ? 'bg-blue-600 text-white' : 'bg-white'" @click="registerMode='patient'">Patient</button>
          <button class="flex-1 rounded-md border px-3 py-2 text-sm" :class="registerMode==='guardian' ? 'bg-blue-600 text-white' : 'bg-white'" @click="registerMode='guardian'">Guardian + Consent</button>
        </div>

        <!-- Doctor -->
        <div v-if="registerMode==='doctor'" class="mt-4 space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">Identifier</label>
              <input v-model="regDoctorIdentifier" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="e.g., doc-123" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Password</label>
              <input v-model="regDoctorPassword" type="password" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="password" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">First name</label>
              <input v-model="regDoctorFirstName" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Last name</label>
              <input v-model="regDoctorLastName" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
          </div>
        </div>

        <!-- Patient -->
        <div v-else-if="registerMode==='patient'" class="mt-4 space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">Identifier</label>
              <input v-model="regPatientIdentifier" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="e.g., svnr-..." />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Password</label>
              <input v-model="regPatientPassword" type="password" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="password" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">First name</label>
              <input v-model="regPatientFirstName" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Last name</label>
              <input v-model="regPatientLastName" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">Birth date</label>
              <input v-model="regPatientBirthDate" type="date" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Gender</label>
              <select v-model="regPatientGender" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm">
                <option value="unknown">unknown</option>
                <option value="female">female</option>
                <option value="male">male</option>
                <option value="other">other</option>
              </select>
            </div>
          </div>
        </div>

        <!-- Guardian -->
        <div v-else class="mt-4 space-y-3">
          <div>
            <label class="text-sm font-medium text-gray-700">Child Patient FHIR id</label>
            <input v-model="regGuardianPatientId" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="e.g., 12345" />
            <div class="mt-1 text-xs text-gray-500">This is the FHIR resource id (not the patient identifier used for login).</div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">Guardian identifier</label>
              <input v-model="regGuardianIdentifier" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="e.g., g-123" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Password</label>
              <input v-model="regGuardianPassword" type="password" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="password" />
            </div>
          </div>
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">First name</label>
              <input v-model="regGuardianFirstName" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Last name</label>
              <input v-model="regGuardianLastName" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" />
            </div>
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Relationship</label>
            <input v-model="regGuardianRelationship" class="mt-1 h-9 w-full rounded-md border border-gray-300 px-3 text-sm" placeholder="parent / guardian" />
          </div>
        </div>

        <div v-if="regError" class="mt-3 rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-700">{{ regError }}</div>
        <div v-if="regResult" class="mt-3 rounded-md bg-green-50 border border-green-200 p-3 text-sm text-green-800">{{ regResult }}</div>

        <div class="mt-4 flex gap-2">
          <button class="flex-1 rounded-md bg-blue-600 px-3 py-2 text-white hover:bg-blue-700 disabled:opacity-50" :disabled="regLoading" @click="submitRegister">
            Create
          </button>
          <button class="rounded-md border px-3 py-2 text-sm" @click="debugDialogOpen = false">Done</button>
        </div>
      </div>
    </div>
  </div>
</template>
