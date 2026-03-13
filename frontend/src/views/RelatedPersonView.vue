<script setup lang="ts">
import { ref } from 'vue'
import { backendFetch } from '@/api/backend'
import { useRelatedPersonAuth } from '@/auth/auth'

import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import RelatedPersonDashboard from '@/components/RelatedPersonDashboard.vue'

const { rpState, isAuthenticated, login, logout } = useRelatedPersonAuth()

const activeTab = ref<'login' | 'register'>('login')

const loginUsername = ref('')
const loginPassword = ref('')
const loginLoading = ref(false)
const loginError = ref<string | null>(null)

const regUsername = ref('')
const regPassword = ref('')
const regFirstName = ref('')
const regLastName = ref('')
const regBirthDate = ref('')
const regLoading = ref(false)
const regError = ref<string | null>(null)
const regSuccess = ref(false)

const demoAccounts = [
  { username: 'noah.parent.demo', password: 'DemoNoah123!', child: 'Noah Gruber' },
  { username: 'emma.parent.demo', password: 'DemoEmma123!', child: 'Emma Novak' },
  { username: 'lukas.parent.demo', password: 'DemoLukas123!', child: 'Lukas Steiner' },
]

async function submitLogin() {
  loginError.value = null
  if (!loginUsername.value.trim()) {
    loginError.value = 'Username is required.'
    return
  }

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

    const json = (await res.json()) as {
      accessToken?: string
      patientId?: string
      firstName?: string
      lastName?: string
    }

    if (!json.accessToken || !json.patientId) throw new Error('Unexpected login response')

    login(json.accessToken, json.patientId, json.firstName ?? null, json.lastName ?? null)
    loginPassword.value = ''
  } catch (e: any) {
    loginError.value = String(e?.message ?? e)
  } finally {
    loginLoading.value = false
  }
}

function useDemoAccount(username: string, password: string) {
  activeTab.value = 'login'
  loginUsername.value = username
  loginPassword.value = password
}

async function submitRegister() {
  regError.value = null
  regSuccess.value = false

  if (!regUsername.value.trim()) {
    regError.value = 'Username is required.'
    return
  }
  if (!regPassword.value) {
    regError.value = 'Password is required.'
    return
  }

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
  <div class="mx-auto max-w-6xl space-y-6">
    <template v-if="isAuthenticated && rpState.patientId">
      <Card class="bg-white shadow-lg">
        <CardHeader class="border-b bg-gradient-to-r from-purple-50 to-blue-50">
          <div class="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
            <div>
              <CardTitle>
                Welcome{{ rpState.firstName ? `, ${rpState.firstName}` : '' }}
              </CardTitle>
              <CardDescription class="mt-1">
                Related person portal · read-only view for your linked child record
              </CardDescription>
            </div>

            <Button variant="outline" @click="logout">Sign out</Button>
          </div>
        </CardHeader>
        <CardContent class="pt-6">
          <p class="text-sm text-gray-600">
            This portal shows the linked child’s vaccinations, appointments, recommendations, allergies, and encounter history.
          </p>
        </CardContent>
      </Card>

      <RelatedPersonDashboard :patient-id="rpState.patientId" />
    </template>

    <template v-else>
      <Card class="bg-white shadow-lg">
        <CardHeader class="border-b bg-gradient-to-r from-purple-50 to-blue-50">
          <CardTitle>Related Person Portal</CardTitle>
          <CardDescription>For guardians and related contacts</CardDescription>
        </CardHeader>
      </Card>

      <div class="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr),320px]">
        <div class="space-y-6">

      <!-- Tab switcher -->
          <div class="overflow-hidden rounded-lg border border-gray-200 bg-white">
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
          </div>

      <!-- Form -->
          <Card v-if="activeTab === 'login'" class="bg-white shadow-sm">
            <CardHeader>
              <CardTitle>Sign in</CardTitle>
              <CardDescription>Use your related-person username and password.</CardDescription>
            </CardHeader>
            <CardContent class="space-y-4">
              <p v-if="regSuccess" class="rounded bg-green-50 px-3 py-2 text-sm text-green-700">
                Registration successful. You can now sign in.
              </p>

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

              <Button class="w-full" :disabled="loginLoading" @click="submitLogin">
                {{ loginLoading ? 'Signing in…' : 'Sign in' }}
              </Button>
            </CardContent>
          </Card>

          <Card v-else class="bg-white shadow-sm">
            <CardHeader>
              <CardTitle>Register</CardTitle>
              <CardDescription>Creates a related-person login for a child under 18.</CardDescription>
            </CardHeader>
            <CardContent class="space-y-4">
              <div class="grid grid-cols-1 gap-4 md:grid-cols-2">
                <div>
                  <label class="text-sm font-medium text-gray-700">First name</label>
                  <input v-model="regFirstName" class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2" placeholder="e.g. Jane" />
                </div>
                <div>
                  <label class="text-sm font-medium text-gray-700">Last name</label>
                  <input v-model="regLastName" class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2" placeholder="e.g. Doe" />
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

              <Button class="w-full" :disabled="regLoading" @click="submitRegister">
                {{ regLoading ? 'Registering…' : 'Register' }}
              </Button>
            </CardContent>
          </Card>
        </div>

        <Card class="bg-white shadow-sm">
          <CardHeader>
            <CardTitle>Demo accounts</CardTitle>
          </CardHeader>
          <CardContent class="space-y-3">
            <button
              v-for="account in demoAccounts"
              :key="account.username"
              class="w-full rounded-lg border p-3 text-left transition-colors hover:bg-gray-50"
              @click="useDemoAccount(account.username, account.password)"
            >
              <div class="text-gray-900">{{ account.child }}</div>
              <div class="mt-1 text-sm text-gray-600">{{ account.username }}</div>
              <div class="text-xs text-gray-500">{{ account.password }}</div>
            </button>
          </CardContent>
        </Card>
      </div>
      <div class="text-center">
      <router-link to="/login" class="text-sm text-blue-600 hover:underline">
        Practitioner login →
      </router-link>
    </div>
    </template>


  </div>
</template>
