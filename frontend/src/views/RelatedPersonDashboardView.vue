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
  <main class="dashboard-page">
    <section class="header-shell">
      <div class="section-header">
        <div>
          <p class="kicker">Health Portal</p>
          <h1>Family workspace</h1>
          <p class="subtitle">Parents and caregivers can view all linked children in one account.</p>
        </div>
        <div class="toolbar">
          <va-badge color="success" :text="`${state.patientIds.length} linked children`" />
          <va-button preset="secondary" @click="logout">Sign out</va-button>
        </div>
      </div>
    </section>

    <PatientWorkspace
      role="related-person"
      :display-name="state.displayName || state.username || 'Related person'"
      :patient-ids="state.patientIds"
    />
  </main>
</template>

<style scoped>
.dashboard-page {
  max-width: 1320px;
  margin: 0 auto;
  padding: 20px;
}

.header-shell {
  background: linear-gradient(180deg, #f6f9fd 0%, #ffffff 100%);
  border: 1px solid #d7e3f2;
  border-radius: 14px;
  box-shadow: 0 8px 24px rgba(16, 49, 89, 0.06);
  padding: 18px 20px;
  margin-bottom: 14px;
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
  flex-wrap: wrap;
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
  margin: 8px 0 6px;
  color: #103159;
}

.subtitle {
  margin: 0;
  color: #2d4868;
}

.toolbar {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}
</style>
