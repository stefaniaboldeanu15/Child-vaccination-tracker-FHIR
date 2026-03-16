<script setup lang="ts">
import { useRouter } from 'vue-router'
import { startSmartLogin } from '@/utils/smart'

const router = useRouter()

async function goToRegistration(role: 'practitioner' | 'related-person') {
  await router.push(role === 'practitioner' ? '/register/practitioner' : '/register/related-person')
}
</script>

<template>
  <main class="page-shell">
    <div class="hero-grid">
      <va-card class="surface-card hero-card">
        <div class="kicker">Vaccination tracker</div>
        <h1 style="font-size: clamp(2.7rem, 5vw, 4.7rem); line-height: 0.98; margin: 10px 0 18px">
          VaxCare Junior
        </h1>
        <p class="muted" style="font-size: 1.15rem; max-width: 780px">
          A clean Vue 3 + Vuestic portal for pediatric vaccination tracking, with SMART on FHIR standalone launch, a practitioner workspace for search and updates, and a related-person view for all linked children.
        </p>

        <div class="toolbar" style="margin: 22px 0 12px">
          <va-button size="large" @click="startSmartLogin('practitioner')">
            Practitioner sign in
          </va-button>
          <va-button size="large" preset="secondary" @click="startSmartLogin('related-person')">
            Parent sign in
          </va-button>
        </div>

        <div class="toolbar" style="margin: 0 0 28px">
          <va-button preset="secondary" color="primary" @click="goToRegistration('practitioner')">
            Register practitioner
          </va-button>
          <va-button preset="secondary" color="success" @click="goToRegistration('related-person')">
            Register parent or guardian
          </va-button>
        </div>

        <div class="metric-grid">
          <div class="metric">
            <div class="value">SMART</div>
            <div class="label">Authorization code + PKCE</div>
          </div>
          <div class="metric">
            <div class="value">FHIR</div>
            <div class="label">Direct resource search + DTO views</div>
          </div>
          <div class="metric">
            <div class="value">Vuestic</div>
            <div class="label">Clean, lightweight interaction layer</div>
          </div>
        </div>
      </va-card>

      <div class="page-grid">
        <va-card class="surface-card hero-card">
          <div class="kicker">Practitioner portal</div>
          <h2 style="margin: 8px 0 10px">Search, review, add and update child data</h2>
          <p class="muted">
            The practitioner workspace loads child profiles, immunizations, recommendations, care plans, goals, encounters, allergies, consents, communications and related persons through the existing backend endpoints.
          </p>
        </va-card>

        <va-card class="surface-card hero-card">
          <div class="kicker">Related person portal</div>
          <h2 style="margin: 8px 0 10px">All linked children in one account</h2>
          <p class="muted">
            A parent or guardian can sign in once and switch between every child linked to the same related-person username, using the SMART-on-FHIR flow configured in the backend.
          </p>
        </va-card>
      </div>
    </div>

    <div class="page-grid" style="margin-top: 20px; grid-template-columns: repeat(3, minmax(0, 1fr))">
      <va-card class="surface-card hero-card">
        <div class="kicker">Care plan design</div>
        <h3 style="margin: 8px 0 12px">Goals = vaccines left to do</h3>
        <p class="muted">
          Goal resources are surfaced as “vaccines left to do”, grouped by CarePlan so the clinician can update status and due dates without changing the backend DTO layer.
        </p>
      </va-card>

      <va-card class="surface-card hero-card">
        <div class="kicker">Encounter view</div>
        <h3 style="margin: 8px 0 12px">Encounter + immunization + observation</h3>
        <p class="muted">
          Encounter-related data is presented as one clinical story, with linked immunizations, observations, locations and organizations resolved from FHIR references when available.
        </p>
      </va-card>

      <va-card class="surface-card hero-card">
        <div class="kicker">Demo logins</div>
        <h3 style="margin: 8px 0 12px">Quick preview</h3>
        <p class="muted">
          Practitioner: <code class="inline-code">dr.mueller</code> / <code class="inline-code">pwMueller01</code><br />
          Parent: <code class="inline-code">anna.gruber.parent</code> / <code class="inline-code">Parent123!</code>
        </p>
      </va-card>
    </div>
  </main>
</template>
