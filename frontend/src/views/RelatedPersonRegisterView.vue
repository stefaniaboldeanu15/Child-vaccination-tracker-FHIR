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
        <div class="kicker">Related person portal</div>
        <h1 style="margin: 8px 0 12px">Register a parent or guardian account</h1>
        <p class="muted" style="margin: 0 0 22px">
          Create a related-person account linked to one child patient id. After registration, continue with the same SMART on FHIR sign-in flow already used by the family workspace.
        </p>

        <div class="metric-grid" style="margin-bottom: 22px">
          <div class="metric">
            <div class="value">Step 1</div>
            <div class="label">Create related person account</div>
          </div>
          <div class="metric">
            <div class="value">Step 2</div>
            <div class="label">Continue to SMART sign in</div>
          </div>
          <div class="metric">
            <div class="value">Demo</div>
            <div class="label">Child id example: patient-001</div>
          </div>
        </div>

        <div class="record-card" style="margin-bottom: 18px">
          <strong style="display: block; margin-bottom: 10px">Already have an account?</strong>
          <p class="muted" style="margin: 0 0 14px">
            Use the existing parent sign-in. The registration form is for creating a new related-person account linked to a child.
          </p>
          <div class="toolbar">
            <va-button @click="startSmartLogin('related-person')">Parent sign in</va-button>
            <va-button preset="secondary" color="secondary" @click="goHome">Back to home</va-button>
          </div>
        </div>
      </va-card>

      <RegistrationCard role="related-person" />
    </div>
  </main>
</template>
