<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { useSession } from '@/auth/session'
import PatientWorkspace from '@/components/PatientWorkspace.vue'

type Language = 'en' | 'de'

const router = useRouter()
const { state, clear } = useSession()
const language = ref<Language>('en')

const i18n = {
  en: {
    kicker: 'Health Portal',
    title: 'Family workspace',
    subtitle: 'Parents and caregivers can view all linked children in one account.',
    linkedProfiles: 'linked profiles',
    signOut: 'Sign out',
    fallbackRole: 'Related person',
    languageLabel: 'Language switch',
  },
  de: {
    kicker: 'Gesundheitsportal',
    title: 'Familien-Arbeitsbereich',
    subtitle: 'Eltern und Bezugspersonen sehen alle verknüpften Kinder in einem Konto.',
    linkedProfiles: 'verknüpfte Profile',
    signOut: 'Abmelden',
    fallbackRole: 'Bezugs­person',
    languageLabel: 'Sprache wechseln',
  },
} as const

const t = computed(() => i18n[language.value])

function setLanguage(nextLanguage: Language) {
  language.value = nextLanguage
}

async function logout() {
  clear()
  await router.push('/')
}
</script>

<template>
  <main class="dashboard-page theme-official">
    <section class="header-shell">
      <div class="section-header">
        <div class="header-main">
          <svg class="hero-mark" viewBox="0 0 64 64" aria-hidden="true">
            <defs>
              <linearGradient id="dashboardMarkGreenFamily" x1="12" y1="10" x2="52" y2="54" gradientUnits="userSpaceOnUse">
                <stop offset="0" stop-color="#22c55e" />
                <stop offset="1" stop-color="#16a34a" />
              </linearGradient>
            </defs>
            <path class="hero-mark-shield" d="M32 8c-10 0-18 8.2-18 18.3C14 40.2 32 56 32 56s18-15.8 18-29.7C50 16.2 42 8 32 8z" />
            <path class="hero-mark-plus" d="M25 31h14" />
            <path class="hero-mark-plus" d="M32 24v14" />
          </svg>
          <div>
            <p class="kicker">{{ t.kicker }}</p>
            <h1>{{ t.title }}</h1>
            <p class="subtitle">{{ t.subtitle }}</p>
          </div>
        </div>
        <div class="toolbar">
          <div class="language-switch" role="group" :aria-label="t.languageLabel">
            <button
              type="button"
              class="language-btn"
              :class="{ 'is-active': language === 'en' }"
              @click="setLanguage('en')"
            >
              EN
            </button>
            <button
              type="button"
              class="language-btn"
              :class="{ 'is-active': language === 'de' }"
              @click="setLanguage('de')"
            >
              DE
            </button>
          </div>
          <div class="toolbar-main">
            <va-badge color="primary" :text="`${state.patientIds.length} ${t.linkedProfiles}`" />
            <va-button preset="secondary" @click="logout">{{ t.signOut }}</va-button>
          </div>
        </div>
      </div>
    </section>

    <PatientWorkspace
      role="related-person"
      :display-name="state.displayName || state.username || 'Related person'"
      :patient-ids="state.patientIds"
      :language="language"
    />
  </main>
</template>

<style scoped>
.dashboard-page {
  --header-border: rgba(15, 57, 141, 0.2);
  --header-shadow: rgba(15, 57, 141, 0.12);
  --header-end: rgba(228, 243, 255, 0.96);
  --kicker-color: #0a7ea8;
  --title-color: #123f9d;
  --muted-color: #3f5878;
  width: calc(100vw - 40px);
  max-width: none;
  margin-left: calc(50% - 50vw + 20px);
  margin-right: calc(50% - 50vw + 20px);
  padding: 20px;
}

.dashboard-page.theme-soft {
  --header-border: rgba(21, 76, 84, 0.18);
  --header-shadow: rgba(21, 76, 84, 0.09);
  --header-end: rgba(235, 248, 239, 0.92);
  --kicker-color: #188d77;
  --title-color: #18424a;
  --muted-color: #4a6366;
}

.header-shell {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, var(--header-end) 100%);
  border: 1px solid var(--header-border);
  border-radius: 18px;
  box-shadow: 0 12px 30px var(--header-shadow);
  padding: 16px 20px;
  margin-bottom: 14px;
}

@media (max-width: 1400px) {
  .dashboard-page {
    width: calc(100vw - 28px);
    margin-left: calc(50% - 50vw + 14px);
    margin-right: calc(50% - 50vw + 14px);
  }
}

@media (max-width: 768px) {
  .dashboard-page {
    width: calc(100vw - 20px);
    margin-left: calc(50% - 50vw + 10px);
    margin-right: calc(50% - 50vw + 10px);
  }
}

.section-header {
  display: flex;
  justify-content: space-between;
  gap: 14px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.header-main {
  display: flex;
  align-items: center;
  gap: 12px;
}

.language-switch {
  display: inline-flex;
  gap: 6px;
}

.language-btn {
  border: 1px solid rgba(18, 63, 157, 0.34);
  background: #eaf3ff;
  color: #123f9d;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 0.72rem;
  font-weight: 700;
  cursor: pointer;
}

.language-btn.is-active {
  background: #123f9d;
  color: #fff;
}

.hero-mark {
  width: 44px;
  height: 44px;
  flex-shrink: 0;
  filter: drop-shadow(0 6px 12px rgba(22, 163, 74, 0.24));
}

.hero-mark .hero-mark-shield {
  fill: url(#dashboardMarkGreenFamily);
  stroke: #15803d;
  stroke-width: 1.8;
}

.hero-mark .hero-mark-plus {
  fill: none;
  stroke: #ffffff;
  stroke-width: 3.2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.kicker {
  margin: 0;
  color: var(--kicker-color);
  font-weight: 700;
  letter-spacing: 0.07em;
  text-transform: uppercase;
  font-size: 0.72rem;
}

h1 {
  margin: 6px 0 5px;
  color: var(--title-color);
  line-height: 1.2;
}

.subtitle {
  margin: 0;
  color: var(--muted-color);
  font-size: 0.93rem;
  line-height: 1.45;
}

.toolbar {
  display: flex;
  flex-direction: column;
  gap: 6px;
  align-items: flex-end;
  flex-wrap: wrap;
}

.toolbar-main {
  display: flex;
  gap: 10px;
  align-items: center;
}
</style>
