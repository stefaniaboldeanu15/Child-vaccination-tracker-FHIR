<script setup lang="ts">
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { backendFetch } from '@/api/backend'
import { useAuth } from '@/auth/auth'

const router = useRouter()
const { loginAsDoctor } = useAuth()

const identifier = ref('')
const password = ref('')
const loading = ref(false)
const error = ref<string | null>(null)

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

    const json = await res.json() as {
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
    router.push('/doctor')
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="max-w-xl mx-auto space-y-6">
    <div class="rounded-xl border border-gray-200 bg-white p-4">
      <div class="text-xl font-semibold">Sign in</div>
      <div class="text-sm text-gray-600">Practitioner portal</div>
    </div>

    <div class="rounded-xl border border-gray-200 bg-white p-4 space-y-4">
      <div class="space-y-3">
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

        <button
          class="w-full rounded-md bg-blue-600 px-3 py-2 text-white disabled:opacity-60"
          :disabled="loading"
          @click="submit"
        >
          {{ loading ? 'Signing in…' : 'Sign in' }}
        </button>
      </div>
    </div>

    <div class="text-center text-sm text-gray-500">
      Guardian or parent?
      <router-link to="/related-person" class="text-blue-600 hover:underline ml-1">
        Related person portal →
      </router-link>
    </div>
  </div>
</template>
