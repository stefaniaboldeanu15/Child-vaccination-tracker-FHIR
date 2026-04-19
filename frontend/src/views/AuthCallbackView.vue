<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { finishSmartLogin } from '@/utils/smart'

const router = useRouter()
const error = ref<string | null>(null)

onMounted(async () => {
  try {
    const role = await finishSmartLogin(new URLSearchParams(window.location.search))
    await router.replace(role === 'practitioner' ? '/practitioner' : '/related-person')
  } catch (reason) {
    error.value = String(reason)
  }
})

async function goHome() {
  await router.push('/')
}
</script>

<template>
  <main class="page-shell">
    <va-card class="surface-card hero-card" style="max-width: 720px; margin: 64px auto">
      <div class="kicker">SMART callback</div>
      <h1 style="margin: 8px 0 14px">Completing sign in</h1>
      <p v-if="!error" class="muted">The authorization code is being exchanged for a token and the portal session is loading.</p>
      <va-alert v-else color="danger" outline>
        {{ error }}
      </va-alert>
      <div class="toolbar" style="margin-top: 18px">
        <va-button preset="secondary" @click="goHome">Back to home</va-button>
      </div>
    </va-card>
  </main>
</template>
