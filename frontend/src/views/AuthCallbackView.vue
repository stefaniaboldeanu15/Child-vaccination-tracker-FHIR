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
  <main class="callback-page">
    <va-card class="callback-card">
      <p class="kicker">Health Portal</p>
      <h1>Completing sign in</h1>
      <p v-if="!error" class="status-text">
        Authorization is being processed and your vaccination portal session is starting.
      </p>
      <va-alert v-else color="danger" outline>
        {{ error }}
      </va-alert>
      <div class="actions">
        <va-button preset="secondary" @click="goHome">Back to home</va-button>
      </div>
    </va-card>
  </main>
</template>

<style scoped>
.callback-page {
  min-height: calc(100vh - 64px);
  display: grid;
  place-items: center;
  padding: 24px 20px;
  background: linear-gradient(180deg, #f7faff 0%, #ffffff 100%);
}

.callback-card {
  width: min(760px, 100%);
  border: 1px solid #d7e3f2;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(16, 49, 89, 0.06);
}

.kicker {
  margin: 0;
  color: #1c5a93;
  font-weight: 700;
  letter-spacing: 0.02em;
  text-transform: uppercase;
  font-size: 0.8rem;
}

h1 {
  margin: 10px 0 14px;
  color: #103159;
  font-size: clamp(1.6rem, 3vw, 2.1rem);
}

.status-text {
  margin: 0;
  color: #2d4868;
  line-height: 1.55;
}

.actions {
  margin-top: 18px;
}
</style>
