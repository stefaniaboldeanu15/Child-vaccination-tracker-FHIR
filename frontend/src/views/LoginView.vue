<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { backendFetch } from '@/api/backend'
import { useAuth } from '@/auth/auth'

import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'

const router = useRouter()
const { loginAsDoctor } = useAuth()

const identifier = ref('')
const password = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

const demoAccounts = [
  {
    identifier: 'dr.mueller',
    password: 'pwMueller01',
    name: 'Dr. Anna Müller',
  },
  {
    identifier: 'dr.smith',
    password: 'pwSmith01',
    name: 'Dr. Allan Smith',
  },
]

function useDemoAccount(nextIdentifier: string, nextPassword: string) {
  identifier.value = nextIdentifier
  password.value = nextPassword
  error.value = null
}

async function submit() {
  error.value = null
  if (!identifier.value.trim()) {
    error.value = 'Identifier is required.'
    return
  }

  loading.value = true
  try {
    const res = await backendFetch('/api/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        identifier: identifier.value.trim(),
        password: password.value,
      }),
    })

    const json = (await res.json()) as {
      accessToken?: string
      practitioner?: {
        practitionerId: string
        practitionerIdentifier: string
        firstName?: string
        lastName?: string
      }
    }

    if (!json.practitioner?.practitionerId) throw new Error('Unexpected login response')

    loginAsDoctor(identifier.value.trim(), password.value, json.practitioner)
    password.value = ''
    await router.push('/doctor')
  } catch (e: any) {
    error.value = String(e?.message ?? e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="mx-auto max-w-6xl space-y-6">
    <Card class="bg-white shadow-lg">
      <CardHeader class="border-b bg-gradient-to-r from-blue-50 to-cyan-50">
        <CardTitle>Practitioner Portal</CardTitle>
        <CardDescription>For doctors and vaccination staff</CardDescription>
      </CardHeader>
    </Card>

    <div class="grid grid-cols-1 gap-6 lg:grid-cols-[minmax(0,1fr),320px]">
      <Card class="bg-white shadow-sm">
        <CardHeader>
          <CardTitle>Sign in</CardTitle>
          <CardDescription>Use your practitioner identifier and password.</CardDescription>
        </CardHeader>
        <CardContent class="space-y-4">
          <div>
            <label class="text-sm font-medium text-gray-700">Identifier</label>
            <input
              v-model="identifier"
              class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2"
              placeholder="e.g. dr.mueller"
              @keydown.enter="submit"
            />
          </div>

          <div>
            <label class="text-sm font-medium text-gray-700">Password</label>
            <input
              v-model="password"
              type="password"
              class="mt-1 w-full rounded-md border border-gray-300 px-3 py-2"
              placeholder="Enter password"
              @keydown.enter="submit"
            />
          </div>

          <p v-if="error" class="text-sm text-red-600">{{ error }}</p>

          <Button class="w-full" :disabled="loading" @click="submit">
            {{ loading ? 'Signing in…' : 'Sign in' }}
          </Button>
        </CardContent>
      </Card>

      <Card class="bg-white shadow-sm">
        <CardHeader>
          <CardTitle>Demo accounts</CardTitle>
          <CardDescription>Click to prefill the practitioner login.</CardDescription>
        </CardHeader>
        <CardContent class="space-y-3">
          <button
            v-for="account in demoAccounts"
            :key="account.identifier"
            class="w-full rounded-lg border p-3 text-left transition-colors hover:bg-gray-50"
            @click="useDemoAccount(account.identifier, account.password)"
          >
            <div class="text-gray-900">{{ account.name }}</div>
            <div class="mt-1 text-sm text-gray-600">{{ account.identifier }}</div>
            <div class="text-xs text-gray-500">{{ account.password }}</div>
            <div class="mt-2 text-xs text-blue-700">{{ account.focus }}</div>
          </button>
        </CardContent>
      </Card>
    </div>

    <div class="text-center">
      <router-link to="/related-person" class="text-sm text-blue-600 hover:underline">
        Related person portal →
      </router-link>
    </div>
  </div>
</template>
