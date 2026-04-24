<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { startSmartLogin } from '@/utils/smart'
import type { SessionRole } from '@/auth/session'

const props = defineProps<{
  role: SessionRole
  badge: string
  title: string
  subtitle: string
  description: string
}>()

const router = useRouter()

const isPractitioner = computed(() => props.role === 'practitioner')
const signInLabel = computed(() => (isPractitioner.value ? 'Practitioner sign in' : 'Parent sign in'))
const registerLabel = computed(() =>
  isPractitioner.value ? 'Register practitioner' : 'Register parent or guardian'
)

async function signIn() {
  await startSmartLogin(props.role)
}

async function register() {
  await router.push(isPractitioner.value ? '/register/practitioner' : '/register/related-person')
}
</script>

<template>
  <va-card class="surface-card hero-card auth-portal-card" :class="isPractitioner ? 'portal-practitioner' : 'portal-parent'">
    <div class="portal-inner">
      <span class="portal-badge">{{ badge }}</span>
      <h2 class="portal-title">{{ title }}</h2>
      <p class="portal-subtitle">{{ subtitle }}</p>
      <p class="portal-desc">
        {{ description }}
      </p>
      <div class="portal-actions">
        <va-button size="large" block @click="signIn">{{ signInLabel }}</va-button>
        <va-button size="large" block preset="secondary" @click="register">{{ registerLabel }}</va-button>
      </div>
    </div>
  </va-card>
</template>

<style scoped>
.auth-portal-card {
  min-height: 100%;
}

.portal-inner {
  display: grid;
  gap: 14px;
}

.portal-badge {
  display: inline-flex;
  align-items: center;
  width: fit-content;
  padding: 6px 12px;
  border-radius: 9999px;
  font-size: 0.78rem;
  font-weight: 700;
  letter-spacing: 0.06em;
  text-transform: uppercase;
  background: rgba(255, 255, 255, 0.7);
  color: #0f172a;
}

.portal-title {
  margin: 0;
  font-size: clamp(1.35rem, 2vw, 1.7rem);
  line-height: 1.15;
  color: #0f172a;
}

.portal-subtitle {
  margin: 0;
  font-size: 1rem;
  font-weight: 600;
  color: #1e293b;
}

.portal-desc {
  margin: 0;
  color: #475569;
  line-height: 1.6;
}

.portal-actions {
  display: grid;
  gap: 10px;
  margin-top: 6px;
}

.portal-practitioner {
  background: linear-gradient(180deg, rgba(13, 148, 136, 0.08), rgba(255, 255, 255, 0.92));
}

.portal-parent {
  background: linear-gradient(180deg, rgba(2, 132, 199, 0.08), rgba(255, 255, 255, 0.92));
}
</style>
