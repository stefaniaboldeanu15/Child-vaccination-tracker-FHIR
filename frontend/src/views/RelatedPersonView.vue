<script setup lang="ts">
import { ref } from 'vue'
import { backendFetch } from '@/api/backend'
import { useRelatedPersonAuth } from '@/auth/auth'

const { rpState, isAuthenticated, login, logout } = useRelatedPersonAuth()

const activeTab = ref<'login' | 'register'>('login')

// Login
const loginUsername = ref('')
const loginPassword = ref('')
const loginLoading = ref(false)
const loginError = ref<string | null>(null)

async function submitLogin() {
  loginError.value = null
  if (!loginUsername.value.trim()) { loginError.value = 'Username is required.'; return }

  loginLoading.value = true
  try {
    const res = await backendFetch('/api/related-person/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: loginUsername.value.trim(),
        password: loginPassword.value,
      }),
    })
    const json = await res.json() as {
      accessToken?: string
      patientId?: string
      firstName?: string
      lastName?: string
    }
    if (!json.accessToken || !json.patientId) throw new Error('Unexpected login response')
    login(json.accessToken, json.patientId, json.firstName ?? null, json.lastName ?? null)
  } catch (e: any) {
    loginError.value = String(e?.message ?? e)
  } finally {
    loginLoading.value = false
  }
}

// Register
const regUsername = ref('')
const regPassword = ref('')
const regFirstName = ref('')
const regLastName = ref('')
const regBirthDate = ref('')
const regLoading = ref(false)
const regError = ref<string | null>(null)
const regSuccess = ref(false)

async function submitRegister() {
  regError.value = null
  regSuccess.value = false
  if (!regUsername.value.trim()) { regError.value = 'Username is required.'; return }
  if (!regPassword.value) { regError.value = 'Password is required.'; return }

  regLoading.value = true
  try {
    await backendFetch('/api/related-person/auth/register', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        username: regUsername.value.trim(),
        password: regPassword.value,
        firstName: regFirstName.value.trim() || undefined,
        lastName: regLastName.value.trim() || undefined,
        birthDate: regBirthDate.value || undefined,
      }),
    })
    regSuccess.value = true
    regUsername.value = ''
    regPassword.value = ''
    regFirstName.value = ''
    regLastName.value = ''
    regBirthDate.value = ''
    activeTab.value = 'login'
  } catch (e: any) {
    regError.value = String(e?.message ?? e)
  } finally {
    regLoading.value = false
  }
}
</script>

<template>
  <div class="max-w-xl mx-auto space-y-6">

    <!-- Authenticated portal -->
    <template v-if="isAuthenticated">
      <div class="rounded-xl border border-gray-200 bg-white p-4 flex items-center justify-between">
        <div>
          <div class="text-xl font-semibold">
            Welcome{{ rpState.firstName ? `, ${rpState.firstName}` : '' }}
          </div>
          <div class="text-sm text-gray-600">Related Person Portal</div>
        </div>
        <button
          class="rounded-md border border-gray-300 px-3 py-1.5 text-sm hover:bg-gray-50"
          @click="logout"
        >
          Sign out
        </button>
      </div>

      <div class="rounded-xl border border-gray-200 bg-white p-6 space-y-3">
        <div class="text-gray-900">Your linked patient</div>
        <div class="rounded-lg border bg-gray-50 px-4 py-3 space-y-1">
          <div class="text-sm text-gray-500">FHIR Patient ID</div>
          <div class="text-gray-900 font-mono break-all">{{ rpState.patientId }}</div>
        </div>
        <p class="text-sm text-gray-500">
          Contact your practitioner to view or update vaccination records for this patient.
        </p>
      </div>
    </template>

    <!-- Login / Register -->
    <template v-else>
      <div class="rounded-xl border border-gray-200 bg-white p-4">
        <div class="text-xl font-semibold">Related Person Portal</div>
        <div class="text-sm text-gray-600">For guardians and related contacts</div>
      </div>

      <!-- Tab switcher -->
      <div class="flex rounded-lg border border-gray-200 bg-white overflow-hidden">
        <button
          class="flex-1 py-2 text-sm transition-colors"
          :class="activeTab === 'login' ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-50'"
          @click="activeTab = 'login'"
        >
          Sign in
        </button>
        <button
          class="flex-1 py-2 text-sm transition-colors"
          :class="activeTab === 'register' ? 'bg-blue-600 text-white' : 'text-gray-600 hover:bg-gray-50'"
          @click="activeTab = 'register'"
        >
          Register
        </button>
      </div>

      <!-- Login form -->
      <div v-if="activeTab === 'login'" class="rounded-xl border border-gray-200 bg-white p-4 space-y-4">
        <p v-if="regSuccess" class="text-sm text-green-700 bg-green-50 rounded px-3 py-2">
          Registration successful. You can now sign in.
        </p>

        <div class="space-y-3">
          <div>
            <label class="text-sm font-medium text-gray-700">Username</label>
            <input
              v-model="loginUsername"
              class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2"
              placeholder="your username"
              @keydown.enter="submitLogin"
            />
          </div>
          <div>
            <label class="text-sm font-medium text-gray-700">Password</label>
            <input
              v-model="loginPassword"
              type="password"
              class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2"
              placeholder="Enter password"
              @keydown.enter="submitLogin"
            />
          </div>

          <p v-if="loginError" class="text-sm text-red-600">{{ loginError }}</p>

          <button
            class="w-full rounded-md bg-blue-600 px-3 py-2 text-white disabled:opacity-60"
            :disabled="loginLoading"
            @click="submitLogin"
          >
            {{ loginLoading ? 'Signing in…' : 'Sign in' }}
          </button>
        </div>
      </div>

      <!-- Register form -->
      <div v-else class="rounded-xl border border-gray-200 bg-white p-4 space-y-4">
        <div class="space-y-3">
          <div class="grid grid-cols-2 gap-3">
            <div>
              <label class="text-sm font-medium text-gray-700">First name</label>
              <input v-model="regFirstName" class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2" placeholder="e.g., Jane" />
            </div>
            <div>
              <label class="text-sm font-medium text-gray-700">Last name</label>
              <input v-model="regLastName" class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2" placeholder="e.g., Doe" />
            </div>
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700">Birth date</label>
            <input type="date" v-model="regBirthDate" class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2" />
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700">Username <span class="text-red-500">*</span></label>
            <input v-model="regUsername" class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2" placeholder="Choose a username" />
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700">Password <span class="text-red-500">*</span></label>
            <input
              v-model="regPassword"
              type="password"
              class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2"
              placeholder="Choose a password"
            />
          </div>

          <p v-if="regError" class="text-sm text-red-600">{{ regError }}</p>

          <button
            class="w-full rounded-md bg-blue-600 px-3 py-2 text-white disabled:opacity-60"
            :disabled="regLoading"
            @click="submitRegister"
          >
            {{ regLoading ? 'Registering…' : 'Register' }}
          </button>
        </div>
      </div>
    </template>

    <!-- Back to practitioner login -->
    <div class="text-center">
      <router-link to="/login" class="text-sm text-blue-600 hover:underline">
        Practitioner login →
      </router-link>
    </div>
  </div>
</template>
