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
    <div class="section-header" style="margin-bottom: 20px">
      <div>
        <div class="kicker">Related person dashboard</div>
        <h1 style="margin: 4px 0 0">Family workspace</h1>
      </div>
      <div class="toolbar">
        <va-badge color="success" :text="`${state.patientIds.length} linked child(ren)`" />
        <va-button preset="secondary" @click="logout">Sign out</va-button>
      </div>
    </div>

    <PatientWorkspace
      role="related-person"
      :display-name="state.displayName || state.username || 'Related person'"
      :patient-ids="state.patientIds"
    />
  </main>
</template>
