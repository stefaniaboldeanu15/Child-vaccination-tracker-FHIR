<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useSession } from '@/auth/session'
import PatientWorkspace from '@/components/PatientWorkspace.vue'

const router = useRouter()
const { state, clear } = useSession()

async function logout() {
  clear()
  await router.push('/')
}
</script>

<template>
  <main class="page-shell">
    <div class="section-header" style="margin-bottom: 5px">
      <div>
        <div class="kicker">Practitioner dashboard</div>
        <h1 style="margin: 4px 0 0">Clinical workspace</h1>
      </div>
      <div class="toolbar">
        <va-badge color="primary" :text="state.fhirUser || 'Practitioner'" />
        <va-button preset="secondary" @click="logout">Sign out</va-button>
      </div>
    </div>

    <PatientWorkspace
      role="practitioner"
      :display-name="state.displayName || state.username || 'Practitioner'"
      :practitioner-id="state.practitionerId"
    />
  </main>
</template>
