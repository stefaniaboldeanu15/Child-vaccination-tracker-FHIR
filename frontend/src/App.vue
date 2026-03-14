<template>
  <div class="min-h-screen bg-gradient-to-br from-blue-50 via-white to-green-50">
    <div class="container mx-auto px-4 py-8">
      <div class="mb-8">
        <div class="flex items-start justify-between gap-4">
          <div class="text-center flex-1">
            <div class="flex items-center justify-center gap-3 mb-2">
              <Syringe class="w-10 h-10 text-blue-600" />
              <h1 class="text-blue-900">European Vaccination Registry</h1>
            </div>
            <p class="text-gray-600">Secure health records across Europe</p>
          </div>

          <div v-if="isAuthenticated" class="flex items-center gap-2">
            <span class="text-sm text-gray-600">{{ roleLabel }}</span>
            <Button variant="outline" size="sm" @click="handleLogout">Logout</Button>
          </div>
        </div>
      </div>

      <RouterView />
    </div>
  </div>
</template>

<script setup lang="ts">
import { Syringe } from 'lucide-vue-next'
import { computed } from 'vue'
import { RouterView, useRouter } from 'vue-router'
import Button from '@/components/ui/Button.vue'
import { useAuth } from '@/auth/auth'

const router = useRouter()
const { state, isAuthenticated, logout } = useAuth()

const roleLabel = computed(() => {
  const p = state.practitioner
  if (!p) return ''
  const name = [p.firstName, p.lastName].filter(Boolean).join(' ').trim()
  return name ? `Doctor: ${name}` : `Doctor: ${p.practitionerIdentifier}`
})

async function handleLogout() {
  logout()
  await router.push('/login')
}
</script>
