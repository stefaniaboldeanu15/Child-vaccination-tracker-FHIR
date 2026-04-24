<script setup lang="ts">
import { useRouter } from 'vue-router'
import RegistrationCard from '@/components/RegistrationCard.vue'
import { startSmartLogin } from '@/utils/smart'

const router = useRouter()

async function goHome() {
  await router.push('/')
}
</script>

<template>
  <main class="page-shell">
    <div class="page-grid two-columns">
      <va-card class="surface-card hero-card">
        <div class="kicker">Practitioner portal</div>
        <h1 style="margin: 8px 0 12px">Register a practitioner account</h1>
        <div class="metric-grid" style="margin-bottom: 22px">
          <div class="metric">
            <div class="value">Step 1</div>
            <div class="label">Create practitioner account</div>
          </div>
          <div class="metric">
            <div class="value">Step 2</div>
            <div class="label">Continue to SMART sign in</div>
          </div>
        </div>

        <div class="record-card" style="margin-bottom: 18px">
          <strong style="display: block; margin-bottom: 10px">Already have an account?</strong>
          <div class="toolbar">
            <va-button @click="startSmartLogin('practitioner')">Practitioner sign in</va-button>
            <va-button preset="secondary" color="secondary" @click="goHome">Back to home</va-button>
          </div>
        </div>
      </va-card>

      <RegistrationCard role="practitioner" />
    </div>
  </main>
</template>
