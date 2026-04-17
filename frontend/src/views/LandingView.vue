<script setup lang="ts">
// Landing page – VaxCare Junior
import { computed, onBeforeUnmount, onMounted, ref } from 'vue'
import { startSmartLogin } from '@/utils/smart'

type HeroSlide = {
  src: string
  alt: string
  caption: {
    en: string
    de: string
  }
}

type Language = 'en' | 'de'

const language = ref<Language>('en')

const i18n = {
  en: {
    builtWith: 'Built with',
    techSmartTitle: 'SMART on FHIR',
    techSmartDesc: 'Authorization code + PKCE',
    techFhirTitle: 'FHIR API',
    techFhirDesc: 'Direct resource search + DTO views',
    techUiTitle: 'Vuestic UI',
    techUiDesc: 'Lightweight interaction layer',
    title: 'Child Vaccination Portal',
    tagline:
      'Modern vaccination experience inspired by global digital health platforms, designed for clinicians, parents, and children.',
    getStartedTitle: 'Get started',
    getStartedSubtitle: 'Sign in securely with ID Austria',
    login: 'Login',
    loginWithIdAustria: 'Login with ID Austria',
    portalAccess: 'Portal access',
    forClinicians: '',
    practitionerAccess: 'Clinician access',
    practitionerSubtitle: 'Manage records, due doses, and vaccine history for each child.',
    practitionerDesc: '',
    forParents: '',
    familyAccess: 'Parent access',
    familySubtitle: 'View and manage vaccination records for all linked children in one place.',
    familyDesc: '',
    features: 'Features',
    planKicker: 'IMMUNIZATION PLAN',
    planTitle: 'Due vaccines',
    planDesc: 'Check upcoming and overdue vaccinations by child.',
    historyKicker: 'HISTORY',
    historyTitle: 'Record timeline',
    historyDesc: 'See visits, vaccinations, and notes in one view.',
    demoKicker: 'DEMO',
    demoTitle: 'Demo login',
    demoPractitionerLabel: 'Practitioner',
    demoParentLabel: 'Parent',
    trustBadges: ['Timeline', 'Children', 'Records'],
    vaccineTimelineInfoTitle: 'Vaccination deadline',
    vaccineTimelineInfoText: '',
    vaccineTimelineMilestones: [
      '0-12 months — early vaccines',
      '1-2 years — booster period',
      '4-6 years — preschool boosters',
      '7-12 years — school-age follow-up',
      '13-17 years — catch-up vaccines',
    ],
    childrenInfoTitle: 'Children',
    childrenInfoText: 'Manage profiles for each child and check vaccine status separately.',
    recordsInfoTitle: 'Records',
    recordsInfoText: 'View stored vaccination history, dose dates, and record details.',
  },
  de: {
    builtWith: 'Erstellt mit',
    techSmartTitle: 'SMART on FHIR',
    techSmartDesc: 'Authorization code + PKCE',
    techFhirTitle: 'FHIR API',
    techFhirDesc: 'Direct resource search + DTO views',
    techUiTitle: 'Vuestic UI',
    techUiDesc: 'Lightweight interaction layer',
    title: 'Kinder-Impfpass Portal',
    tagline:
      'Moderne Impfverwaltung im Stil internationaler eHealth-Portale, für Fachpersonal, Eltern und Kinder.',
    chooseSignIn: 'Anmeldung auswählen',
    getStartedTitle: 'Jetzt starten',
    getStartedSubtitle: 'Sicher mit ID Austria anmelden',
    login: 'Anmelden',
    loginWithIdAustria: 'Login mit ID Austria',
    portalAccess: 'Portalzugang',
    forClinicians: '',
    practitionerAccess: 'Zugang für Fachpersonal',
    practitionerSubtitle: 'Verwalten Sie Akten, fällige Dosen und den Impfverlauf für jedes Kind.',
    practitionerDesc: '',
    forParents: '',
    familyAccess: 'Zugang für Eltern',
    familySubtitle: 'Sehen und verwalten Sie Impfunterlagen aller verknüpften Kinder an einem Ort.',
    familyDesc: '',
    features: 'Funktionen',
    planKicker: 'IMPFPLAN',
    planTitle: 'Fällige Impfungen',
    planDesc: 'Prüfen Sie kommende und überfällige Impfungen pro Kind.',
    historyKicker: 'VERLAUF',
    historyTitle: 'Dokumentations-Timeline',
    historyDesc: 'Sehen Sie Besuche, Impfungen und Notizen in einer Ansicht.',
    demoKicker: 'DEMO',
    demoTitle: 'Demo-Anmeldung',
    demoPractitionerLabel: 'Fachpersonal',
    demoParentLabel: 'Eltern',
    trustBadges: ['Impf-Timeline', 'Kinder', 'Unterlagen'],
    vaccineTimelineInfoTitle: 'Impf-Fristen',
    vaccineTimelineInfoText: '',
    vaccineTimelineMilestones: [
      '0-12 Monate — frühe Impfungen',
      '1-2 Jahre — Auffrischungsphase',
      '4-6 Jahre — Vorschul-Auffrischungen',
      '7-12 Jahre — Nachverfolgung im Schulalter',
      '13-17 Jahre — Nachholimpfungen',
    ],
    childrenInfoTitle: 'Kinder',
    childrenInfoText: 'Verwalten Sie Profile je Kind und prüfen Sie den Impfstatus getrennt.',
    recordsInfoTitle: 'Unterlagen',
    recordsInfoText: 'Sehen Sie gespeicherte Impfverläufe, Dosisdaten und Dokumentdetails.',
  },
} as const

const t = computed(() => i18n[language.value])

const heroSlides: HeroSlide[] = [
  {
    src: 'https://images.unsplash.com/photo-1576091160399-112ba8d25d1d?auto=format&fit=crop&w=1600&q=80',
    alt: 'Doctor reviewing vaccination documents with parent and child',
    caption: {
      en: 'Vaccination records and care planning',
      de: 'Impfdokumentation und Versorgungsplanung',
    },
  },
  {
    src: 'https://images.unsplash.com/photo-1666214280557-f1b5022eb634?auto=format&fit=crop&w=1600&q=80',
    alt: 'Doctor consulting with a child and parent',
    caption: {
      en: 'Doctor, child, and parent consultation',
      de: 'Arztgespräch mit Kind und Elternteil',
    },
  },
  {
    src: 'https://images.unsplash.com/photo-1624727828489-a1e03b79bba8?auto=format&fit=crop&w=1600&q=80',
    alt: 'Family with child at pediatric clinic',
    caption: {
      en: 'Family-centered pediatric care',
      de: 'Familienorientierte Kinderheilkunde',
    },
  },
  {
    src: 'https://images.unsplash.com/photo-1631815588090-d4bfec5b1ccb?auto=format&fit=crop&w=1600&q=80',
    alt: 'Pediatric doctor in hospital corridor',
    caption: {
      en: 'Hospital-ready pediatric support',
      de: 'Padiatrische Versorgung im Krankenhaus',
    },
  },
  {
    src: 'https://images.unsplash.com/photo-1584515979956-d9f6e5d09982?auto=format&fit=crop&w=1600&q=80',
    alt: 'Medical team discussing records in clinic office',
    caption: {
      en: 'Clinical office and register workflow',
      de: 'Klinik- und Register-Workflow',
    },
  },
  {
    src: 'https://images.unsplash.com/photo-1576765608622-067973a79f53?auto=format&fit=crop&w=1600&q=80',
    alt: 'Nurse and mother with child during vaccination visit',
    caption: {
      en: 'Safe vaccination visits for children',
      de: 'Sichere Impftermine für Kinder',
    },
  },
  {
    src: 'https://images.unsplash.com/photo-1516549655169-df83a0774514?auto=format&fit=crop&w=1600&q=80',
    alt: 'Mother and child with pediatric doctor',
    caption: {
      en: 'Trusted care for families',
      de: 'Vertrauensvolle Betreuung für Familien',
    },
  },
]

const activeSlide = ref(0)
let slideTimer: ReturnType<typeof setInterval> | null = null

function setSlide(index: number) {
  activeSlide.value = index
}

function setLanguage(nextLanguage: Language) {
  language.value = nextLanguage
}

onMounted(() => {
  slideTimer = setInterval(() => {
    activeSlide.value = (activeSlide.value + 1) % heroSlides.length
  }, 2600)
})

onBeforeUnmount(() => {
  if (slideTimer) clearInterval(slideTimer)
})
</script>

<template>
  <main class="page-shell landing-page">
    <div class="needle-bg" aria-hidden="true">
      <span v-for="i in 24" :key="i" class="needle-icon">💉</span>
    </div>

    <!-- Hero -->
    <section class="landing-hero">
      <va-card class="surface-card hero-card hero-main">
        <header class="landing-header">
          <div class="language-switch" role="group" aria-label="Language switch">
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
          <svg class="hero-mark" viewBox="0 0 64 64" aria-hidden="true">
            <defs>
              <linearGradient id="heroMarkGreen" x1="12" y1="10" x2="52" y2="54" gradientUnits="userSpaceOnUse">
                <stop offset="0" stop-color="#22c55e" />
                <stop offset="1" stop-color="#16a34a" />
              </linearGradient>
            </defs>
            <path class="hero-mark-shield" d="M32 8c-10 0-18 8.2-18 18.3C14 40.2 32 56 32 56s18-15.8 18-29.7C50 16.2 42 8 32 8z" />
            <path class="hero-mark-plus" d="M25 31h14" />
            <path class="hero-mark-plus" d="M32 24v14" />
          </svg>
          <div class="header-text">
            <h1 class="landing-title">{{ t.title }}</h1>
            <p class="landing-tagline">{{ t.tagline }}</p>
          </div>
        </header>
        <div class="hero-carousel" aria-label="Families, kids, and healthcare">
          <figure class="carousel-stage">
            <img
              v-for="(slide, index) in heroSlides"
              :key="slide.src"
              class="carousel-image"
              :class="{ 'is-active': index === activeSlide }"
              :src="slide.src"
              :alt="slide.alt"
            />
          </figure>
          <div class="carousel-dots" aria-label="Select slide">
            <button
              v-for="(slide, index) in heroSlides"
              :key="slide.alt"
              type="button"
              class="carousel-dot"
              :class="{ 'is-active': index === activeSlide }"
              :aria-label="`Show slide ${index + 1}`"
              @click="setSlide(index)"
            />
          </div>
        </div>
        <div class="trust-badges">
          <span class="badge-popover-wrap">
            <span class="trust-badge">
              <svg class="badge-icon-syringe" viewBox="0 0 24 24" aria-hidden="true">
                <path d="M15.8 3.8l4.4 4.4-1.8 1.8-4.4-4.4z" />
                <path d="M5 16.1l8.4-8.4 3.6 3.6-8.4 8.4H5z" />
                <path d="M3 20h3" />
                <path d="M20 4l-1.2 1.2" />
              </svg>
              {{ t.trustBadges[0] }}
            </span>
            <span class="timeline-popover" role="tooltip">
              <strong>{{ t.vaccineTimelineInfoTitle }}</strong>
              <span v-if="t.vaccineTimelineInfoText">{{ t.vaccineTimelineInfoText }}</span>
              <ul>
                <li v-for="item in t.vaccineTimelineMilestones" :key="item">{{ item }}</li>
              </ul>
            </span>
          </span>
          <span class="badge-popover-wrap">
            <span class="trust-badge">👨‍👩‍👧 {{ t.trustBadges[1] }}</span>
            <span class="timeline-popover compact-popover" role="tooltip">
              <strong>{{ t.childrenInfoTitle }}</strong>
              <span>{{ t.childrenInfoText }}</span>
            </span>
          </span>
          <span class="badge-popover-wrap">
            <span class="trust-badge">📋 {{ t.trustBadges[2] }}</span>
            <span class="timeline-popover compact-popover" role="tooltip">
              <strong>{{ t.recordsInfoTitle }}</strong>
              <span>{{ t.recordsInfoText }}</span>
            </span>
          </span>
        </div>
        <div class="hero-actions">
          <p class="hero-actions-title">{{ t.getStartedTitle }}</p>
          <p class="hero-actions-label">{{ t.getStartedSubtitle }}</p>
          <div class="toolbar landing-toolbar">
            <va-button size="large" class="btn-practitioner" @click="startSmartLogin('practitioner')">
              {{ t.login }}
            </va-button>
            <va-button size="large" preset="secondary" class="btn-idaustria" disabled>
              {{ t.loginWithIdAustria }}
            </va-button>
          </div>
        </div>
        <p class="hero-actions-title built-with-title">{{ t.builtWith }}</p>
        <div class="metric-grid">
          <div class="metric metric-smart">
            <div class="value">{{ t.techSmartTitle }}</div>
            <div class="label">{{ t.techSmartDesc }}</div>
          </div>
          <div class="metric metric-fhir">
            <div class="value">{{ t.techFhirTitle }}</div>
            <div class="label">{{ t.techFhirDesc }}</div>
          </div>
          <div class="metric metric-vuestic">
            <div class="value">{{ t.techUiTitle }}</div>
            <div class="label">{{ t.techUiDesc }}</div>
          </div>
        </div>
      </va-card>
    </section>

    <!-- Who is this for -->
    <section class="landing-section portals-section">
      <h2 class="section-title">{{ t.portalAccess }}</h2>
      <div class="portals-grid">
        <va-card class="surface-card hero-card portal-card portal-practitioner">
          <div class="portal-inner">
            <span v-if="t.forClinicians" class="portal-badge">{{ t.forClinicians }}</span>
            <h2 class="portal-title">{{ t.practitionerAccess }}</h2>
            <p class="portal-subtitle">{{ t.practitionerSubtitle }}</p>
            <p v-if="t.practitionerDesc" class="portal-desc">{{ t.practitionerDesc }}</p>
          </div>
        </va-card>
        <va-card class="surface-card hero-card portal-card portal-parent">
          <div class="portal-inner">
            <span v-if="t.forParents" class="portal-badge">{{ t.forParents }}</span>
            <h2 class="portal-title">{{ t.familyAccess }}</h2>
            <p class="portal-subtitle">{{ t.familySubtitle }}</p>
            <p v-if="t.familyDesc" class="portal-desc">{{ t.familyDesc }}</p>
          </div>
        </va-card>
      </div>
    </section>

    <!-- What you can do -->
    <section class="landing-section features-section">
      <h2 class="section-title">{{ t.features }}</h2>
      <div class="features-grid">
        <va-card class="surface-card hero-card feature-card">
          <div class="kicker">{{ t.planKicker }}</div>
          <h3 class="feature-title">{{ t.planTitle }}</h3>
          <p class="feature-desc">{{ t.planDesc }}</p>
        </va-card>
        <va-card class="surface-card hero-card feature-card">
          <div class="kicker">{{ t.historyKicker }}</div>
          <h3 class="feature-title">{{ t.historyTitle }}</h3>
          <p class="feature-desc">{{ t.historyDesc }}</p>
        </va-card>
        <va-card class="surface-card hero-card feature-card demo-card">
          <div class="kicker">{{ t.demoKicker }}</div>
          <h3 class="feature-title">{{ t.demoTitle }}</h3>
          <p class="feature-desc">
            {{ t.demoPractitionerLabel }}: <code class="inline-code">dr.mueller</code> / <code class="inline-code">pwMueller01</code><br />
            {{ t.demoParentLabel }}: <code class="inline-code">anna.gruber.parent</code> / <code class="inline-code">Parent123!</code>
          </p>
        </va-card>
      </div>
    </section>
  </main>
</template>

<style scoped>
.landing-page {
  --landing-accent: #166534;
  --landing-accent-soft: rgba(22, 101, 52, 0.1);
  --landing-blue: #1d4ed8;
  --landing-blue-soft: rgba(29, 78, 216, 0.12);
  --landing-teal: #16a34a;
  --landing-mint: #22c55e;
  --landing-violet: #1e40af;
  --landing-amber: #16a34a;
  --radius-lg: clamp(14px, 2.5vw, 20px);
  --radius-md: clamp(12px, 1.5vw, 16px);
  --radius-pill: 9999px;
  --ease-out: cubic-bezier(0.22, 1, 0.36, 1);
  --ease-spring: cubic-bezier(0.34, 1.56, 0.64, 1);
  --glass: rgba(255, 255, 255, 0.88);
  --glass-border: rgba(0, 0, 0, 0.06);
  --shadow-soft: 0 1px 3px rgba(0, 0, 0, 0.05), 0 4px 12px rgba(0, 0, 0, 0.05);
  --shadow-hover: 0 4px 12px rgba(0, 0, 0, 0.08), 0 12px 28px rgba(13, 148, 136, 0.08);
  --space-xs: clamp(6px, 1vw, 10px);
  --space-sm: clamp(10px, 2vw, 16px);
  --space-md: clamp(14px, 2.5vw, 20px);
  --space-lg: clamp(20px, 3vw, 32px);
  --space-xl: clamp(24px, 4vw, 36px);
  --text-primary: #0f172a;
  --text-secondary: #14532d;
  --text-muted: #166534;
  --btn-accent: #1d4ed8;
  --btn-accent-hover: #1e40af;
}

.landing-page {
  padding: var(--space-lg) var(--space-md) var(--space-xl);
  max-width: min(1440px, 100%);
  margin-left: auto;
  margin-right: auto;
}

.landing-page::before {
  content: "";
  position: fixed;
  inset: 0;
  background:
    radial-gradient(ellipse 100% 60% at 50% -10%, rgba(22, 101, 52, 0.11), transparent 50%),
    radial-gradient(ellipse 70% 50% at 100% 20%, rgba(34, 197, 94, 0.1), transparent 45%),
    radial-gradient(ellipse 60% 40% at 0% 60%, rgba(22, 163, 74, 0.08), transparent 45%);
  pointer-events: none;
  z-index: 0;
}

.needle-bg {
  position: fixed;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
}

.needle-icon {
  position: absolute;
  font-size: clamp(0.85rem, 1.5vw, 1.2rem);
  opacity: 0.06;
  animation: needle-float 12s ease-in-out infinite;
}

.needle-icon:nth-child(1) { left: 5%; top: 8%; animation-delay: 0s; }
.needle-icon:nth-child(2) { left: 18%; top: 15%; animation-delay: -1.5s; transform: rotate(-12deg); }
.needle-icon:nth-child(3) { left: 28%; top: 6%; animation-delay: -3s; transform: rotate(8deg); }
.needle-icon:nth-child(4) { left: 42%; top: 12%; animation-delay: -0.5s; transform: rotate(-5deg); }
.needle-icon:nth-child(5) { left: 55%; top: 5%; animation-delay: -2s; }
.needle-icon:nth-child(6) { left: 68%; top: 14%; animation-delay: -4s; transform: rotate(10deg); }
.needle-icon:nth-child(7) { left: 82%; top: 9%; animation-delay: -1s; transform: rotate(-8deg); }
.needle-icon:nth-child(8) { left: 92%; top: 18%; animation-delay: -2.5s; }
.needle-icon:nth-child(9) { left: 8%; top: 35%; animation-delay: -3.5s; transform: rotate(6deg); }
.needle-icon:nth-child(10) { left: 22%; top: 42%; animation-delay: -0.8s; transform: rotate(-10deg); }
.needle-icon:nth-child(11) { left: 75%; top: 38%; animation-delay: -2.2s; transform: rotate(4deg); }
.needle-icon:nth-child(12) { left: 88%; top: 45%; animation-delay: -4.5s; transform: rotate(-6deg); }
.needle-icon:nth-child(13) { left: 12%; top: 62%; animation-delay: -1.2s; transform: rotate(12deg); }
.needle-icon:nth-child(14) { left: 35%; top: 68%; animation-delay: -3.2s; transform: rotate(-4deg); }
.needle-icon:nth-child(15) { left: 58%; top: 72%; animation-delay: -0.3s; transform: rotate(7deg); }
.needle-icon:nth-child(16) { left: 78%; top: 65%; animation-delay: -2.8s; transform: rotate(-9deg); }
.needle-icon:nth-child(17) { left: 5%; top: 82%; animation-delay: -4.2s; transform: rotate(5deg); }
.needle-icon:nth-child(18) { left: 45%; top: 88%; animation-delay: -1.8s; transform: rotate(-7deg); }
.needle-icon:nth-child(19) { left: 65%; top: 92%; animation-delay: -3.8s; transform: rotate(3deg); }
.needle-icon:nth-child(20) { left: 90%; top: 78%; animation-delay: -0.6s; transform: rotate(-11deg); }
.needle-icon:nth-child(21) { left: 50%; top: 28%; animation-delay: -2.3s; transform: rotate(9deg); opacity: 0.08; }
.needle-icon:nth-child(22) { left: 30%; top: 55%; animation-delay: -4s; transform: rotate(-3deg); opacity: 0.08; }
.needle-icon:nth-child(23) { left: 70%; top: 52%; animation-delay: -1.4s; transform: rotate(6deg); opacity: 0.08; }
.needle-icon:nth-child(24) { left: 15%; top: 78%; animation-delay: -3.3s; transform: rotate(-8deg); opacity: 0.08; }

@keyframes needle-float {
  0%, 100% { opacity: 0.05; }
  50% { opacity: 0.1; }
}

.landing-page .landing-hero {
  position: relative;
  z-index: 1;
  margin-bottom: var(--space-lg);
}

.landing-page .landing-section {
  position: relative;
  z-index: 1;
  margin-top: var(--space-lg);
}

.landing-page .section-title {
  font-size: clamp(0.85rem, 1.5vw, 1.1rem);
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
  color: var(--landing-accent);
  margin: 0 0 var(--space-md);
}

/* Flexible grids: wrap when space is tight */
.landing-page .portals-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(320px, 100%), 1fr));
  gap: var(--space-md);
}

.landing-page .portal-inner {
  padding: 4px 0;
}

.landing-page .portal-badge {
  display: inline-block;
  font-size: clamp(0.7rem, 1.2vw, 0.75rem);
  font-weight: 600;
  letter-spacing: 0.08em;
  text-transform: uppercase;
  padding: var(--space-xs) var(--space-sm);
  border-radius: var(--radius-pill);
  margin-bottom: var(--space-sm);
}

.landing-page .portal-practitioner .portal-badge {
  background: rgba(2, 132, 199, 0.15);
  color: var(--landing-blue);
}

.landing-page .portal-parent .portal-badge {
  background: rgba(5, 150, 105, 0.15);
  color: var(--landing-mint);
}

.landing-page .portal-card .portal-title {
  font-size: clamp(1.1rem, 2vw, 1.25rem);
  font-weight: 700;
  color: #1d4ed8;
  margin: 0 0 var(--space-xs);
}

.landing-page .portal-subtitle {
  font-size: clamp(0.9rem, 1.5vw, 1rem);
  font-weight: 600;
  color: #0f172a;
  margin: 0 0 var(--space-sm);
}

.landing-page .portal-desc {
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  line-height: 1.55;
  color: #111827;
  margin: 0;
}

.landing-page .features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(260px, 100%), 1fr));
  gap: var(--space-md);
}

.landing-page .hero-main {
  padding: var(--space-md) var(--space-md) var(--space-lg) !important;
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.98) 0%, rgba(239, 246, 255, 0.92) 100%);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid rgba(29, 78, 216, 0.18);
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 28px rgba(29, 78, 216, 0.12);
  transition: box-shadow 0.4s var(--ease-out), transform 0.4s var(--ease-out), border-color 0.3s ease;
}

.landing-page .hero-main:hover {
  box-shadow: var(--shadow-hover);
}

.landing-page .landing-header {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: var(--space-sm);
  margin-bottom: var(--space-md);
  position: relative;
}

.landing-page .language-switch {
  position: absolute;
  top: 0;
  right: 0;
  display: inline-flex;
  gap: 6px;
  z-index: 2;
}

.landing-page .language-btn {
  border: 1px solid rgba(29, 78, 216, 0.3);
  background: #eff6ff;
  color: #1d4ed8;
  border-radius: 999px;
  padding: 4px 10px;
  font-size: 0.72rem;
  font-weight: 700;
  cursor: pointer;
}

.landing-page .language-btn.is-active {
  background: #1d4ed8;
  color: #fff;
}

.landing-page .hero-mark {
  width: clamp(46px, 8vw, 62px);
  height: clamp(46px, 8vw, 62px);
  flex-shrink: 0;
  filter: drop-shadow(0 6px 12px rgba(22, 163, 74, 0.24));
}

.landing-page .hero-mark .hero-mark-shield {
  fill: url(#heroMarkGreen);
  stroke: #15803d;
  stroke-width: 1.8;
}

.landing-page .hero-mark .hero-mark-plus {
  fill: none;
  stroke: #ffffff;
  stroke-width: 3.2;
  stroke-linecap: round;
  stroke-linejoin: round;
}

.landing-page .landing-header .header-text {
  min-width: 0;
}

.landing-page .landing-title {
  font-size: clamp(1.65rem, 3.5vw, 2.5rem);
  line-height: 1.15;
  margin: 0 0 var(--space-xs);
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.landing-page .landing-tagline {
  font-size: clamp(0.95rem, 1.5vw, 1.05rem);
  line-height: 1.35;
  margin: 0;
  max-width: none;
  white-space: nowrap;
  color: var(--text-secondary);
  font-weight: 500;
  letter-spacing: 0.01em;
}

.landing-page .hero-actions {
  margin: var(--space-md) 0;
  padding: var(--space-sm) 0;
  border-top: 1px solid var(--glass-border);
  border-bottom: 1px solid var(--glass-border);
}

.landing-page .hero-carousel {
  margin-bottom: var(--space-sm);
}

.landing-page .carousel-stage {
  margin: 0;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid rgba(29, 78, 216, 0.2);
  box-shadow: 0 8px 24px rgba(29, 78, 216, 0.16);
  position: relative;
  min-height: 360px;
}

.landing-page .carousel-image {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  object-fit: cover;
  display: block;
  opacity: 0;
  transform: scale(1.04);
  transition: opacity 480ms ease, transform 2200ms ease;
}

.landing-page .carousel-image.is-active {
  opacity: 1;
  transform: scale(1.01);
}

.landing-page .carousel-stage::after {
  content: "";
  position: absolute;
  inset: 0;
  background: transparent;
}

.landing-page .carousel-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 10px;
}

.landing-page .carousel-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  border: 1px solid rgba(29, 78, 216, 0.35);
  background: rgba(29, 78, 216, 0.18);
  cursor: pointer;
  transition: transform 180ms ease, background-color 180ms ease;
}

.landing-page .carousel-dot.is-active {
  background: rgba(29, 78, 216, 0.95);
  transform: scale(1.15);
}

.landing-page .trust-badges {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: var(--space-sm);
}

.landing-page .trust-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  background: linear-gradient(135deg, rgba(219, 234, 254, 0.88), rgba(239, 246, 255, 0.94));
  border: 1px solid rgba(29, 78, 216, 0.26);
  color: #1e3a8a;
  border-radius: 999px;
  padding: 6px 10px;
  font-size: 0.78rem;
  font-weight: 600;
}

.landing-page .badge-popover-wrap {
  position: relative;
  display: inline-flex;
}

.landing-page .timeline-popover {
  position: absolute;
  left: 0;
  top: calc(100% + 10px);
  width: min(360px, 78vw);
  background: #ffffff;
  border: 1px solid rgba(29, 78, 216, 0.25);
  border-radius: 12px;
  box-shadow: 0 16px 32px rgba(2, 6, 23, 0.14);
  padding: 12px 14px;
  display: flex;
  flex-direction: column;
  gap: 6px;
  color: #0f172a;
  z-index: 8;
  opacity: 0;
  transform: translateY(6px);
  pointer-events: none;
  transition: opacity 0.18s ease, transform 0.18s ease;
}

.landing-page .timeline-popover::before {
  content: "";
  position: absolute;
  top: -6px;
  left: 20px;
  width: 10px;
  height: 10px;
  background: #fff;
  border-top: 1px solid rgba(29, 78, 216, 0.25);
  border-left: 1px solid rgba(29, 78, 216, 0.25);
  transform: rotate(45deg);
}

.landing-page .timeline-popover strong {
  color: #1d4ed8;
  font-size: 0.84rem;
}

.landing-page .timeline-popover span {
  font-size: 0.78rem;
  line-height: 1.4;
  color: #1f2937;
}

.landing-page .compact-popover {
  width: min(320px, 74vw);
}

.landing-page .timeline-popover ul {
  margin: 2px 0 0;
  padding-left: 16px;
  font-size: 0.75rem;
  line-height: 1.45;
  color: #111827;
}

.landing-page .badge-popover-wrap:hover .timeline-popover {
  opacity: 1;
  transform: translateY(0);
}

.landing-page .badge-icon-syringe {
  width: 13px;
  height: 13px;
  stroke: currentColor;
  stroke-width: 1.8;
  fill: none;
  stroke-linecap: round;
  stroke-linejoin: round;
  transform: rotate(-18deg);
  flex-shrink: 0;
}

.landing-page .hero-actions-label {
  font-size: 0.8rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--text-muted);
  margin: 0 0 var(--space-sm);
}

.landing-page .hero-actions-title {
  margin: 0 0 4px;
  font-size: 1.02rem;
  font-weight: 700;
  color: #1d4ed8;
}

.landing-page .built-with-title {
  margin-top: 4px;
}

.landing-page .role-select-wrap {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  font-size: 0.86rem;
  color: var(--text-secondary);
  background: rgba(29, 78, 216, 0.08);
  border: 1px solid rgba(29, 78, 216, 0.2);
  border-radius: 999px;
  padding: 6px 10px;
}

.landing-page .role-select {
  border: none;
  background: transparent;
  color: var(--text-primary);
  font-weight: 600;
  outline: none;
}

.landing-page :deep(.va-button.btn-idaustria) {
  border-radius: var(--radius-pill) !important;
  min-height: 44px;
}

.landing-page .landing-toolbar {
  margin: 0 0 var(--space-sm);
  gap: var(--space-sm);
  flex-wrap: wrap;
}

.landing-page .metric-grid {
  gap: var(--space-sm);
  grid-template-columns: repeat(auto-fit, minmax(min(140px, 100%), 1fr));
  margin-top: var(--space-md);
}

.landing-page .metric {
  padding: var(--space-md);
  border-radius: var(--radius-md);
  min-width: 0;
  border: 1px solid rgba(22, 101, 52, 0.2);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.99) 0%, rgba(240, 253, 244, 0.9) 100%);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 6px 18px rgba(21, 128, 61, 0.12);
  transition: transform 0.35s var(--ease-spring), box-shadow 0.35s var(--ease-out);
}

.landing-page .metric:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.landing-page .metric .value {
  font-size: clamp(1.25rem, 2vw, 1.5rem);
  font-weight: 800;
  color: #1d4ed8;
  letter-spacing: -0.02em;
}

.landing-page .metric .label {
  font-size: clamp(0.8rem, 1.2vw, 0.9rem);
  color: #111827;
  margin-top: var(--space-xs);
}

.landing-page .metric-smart {
  border-color: rgba(22, 101, 52, 0.3);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.99), rgba(236, 253, 245, 0.9)) !important;
}
.landing-page .metric-smart .value { color: #1d4ed8; }
.landing-page .metric-fhir {
  border-color: rgba(22, 163, 74, 0.3);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.99), rgba(236, 253, 245, 0.9)) !important;
}
.landing-page .metric-fhir .value { color: #15803d; }
.landing-page .metric-vuestic {
  border-color: rgba(20, 83, 45, 0.28);
  background: linear-gradient(135deg, rgba(255, 255, 255, 0.99), rgba(236, 253, 245, 0.9)) !important;
}
.landing-page .metric-vuestic .value { color: #1e3a8a; }

.landing-page .btn-icon {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 1.25em;
  height: 1.25em;
  margin-right: 0.35em;
  flex-shrink: 0;
}
.landing-page .btn-icon svg {
  width: 100%;
  height: 100%;
}

.landing-page :deep(.va-button.btn-practitioner) {
  background: var(--btn-accent) !important;
  color: #fff !important;
  border: none !important;
  font-weight: 600 !important;
  border-radius: var(--radius-pill) !important;
  padding: var(--space-sm) var(--space-md) !important;
  min-height: 44px;
  box-shadow: 0 2px 10px rgba(13, 148, 136, 0.35) !important;
  transition: box-shadow 0.25s var(--ease-out), transform 0.25s var(--ease-out), background 0.2s ease !important;
}
.landing-page :deep(.va-button.btn-practitioner .btn-icon) {
  color: #fff;
}

.landing-page :deep(.va-button.btn-practitioner:hover) {
  background: var(--btn-accent-hover) !important;
  box-shadow: 0 4px 14px rgba(13, 148, 136, 0.4) !important;
  transform: translateY(-1px) !important;
}

.landing-page :deep(.va-button.btn-idaustria) {
  color: var(--btn-accent) !important;
  border: 2px solid var(--btn-accent) !important;
  font-weight: 600 !important;
  background: rgba(255, 255, 255, 0.85) !important;
  backdrop-filter: blur(8px) !important;
  border-radius: var(--radius-pill) !important;
  padding: var(--space-sm) var(--space-md) !important;
  min-height: 44px;
  transition: background 0.3s ease, border-color 0.3s ease, transform 0.3s var(--ease-spring), color 0.2s ease !important;
}

.landing-page :deep(.va-button.btn-idaustria:hover) {
  background: rgba(37, 99, 235, 0.1) !important;
  border-color: var(--btn-accent-hover) !important;
  color: var(--btn-accent-hover) !important;
  transform: translateY(-1px) !important;
}

.landing-page .portal-practitioner {
  border-left: 5px solid var(--landing-blue);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(240, 253, 244, 0.9)) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.landing-page .portal-parent {
  border-left: 5px solid var(--landing-mint);
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(240, 253, 244, 0.9)) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}
.landing-page .portal-card {
  border: 1px solid rgba(22, 101, 52, 0.16);
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 24px rgba(21, 128, 61, 0.1);
  padding: var(--space-md) !important;
  min-width: 0;
  transition: box-shadow 0.4s var(--ease-out), transform 0.4s var(--ease-spring);
}

.landing-page .portal-card:hover {
  box-shadow: 0 12px 30px rgba(21, 128, 61, 0.16);
  transform: translateY(-3px);
}


.landing-page .feature-card {
  border: 1px solid rgba(22, 101, 52, 0.16);
  border-radius: var(--radius-lg);
  box-shadow: 0 10px 24px rgba(21, 128, 61, 0.1);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.99) 0%, rgba(240, 253, 244, 0.9) 100%) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  min-width: 0;
  transition: box-shadow 0.4s var(--ease-out), transform 0.4s var(--ease-spring);
}

.landing-page .feature-card:nth-child(1) {
  border-left: 5px solid #16a34a;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(240, 253, 244, 0.9)) !important;
}
.landing-page .feature-card:nth-child(2) {
  border-left: 5px solid #15803d;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(240, 253, 244, 0.9)) !important;
}
.landing-page .feature-card:nth-child(3) {
  border-left: 5px solid #14532d;
  background: linear-gradient(145deg, rgba(255, 255, 255, 0.99), rgba(240, 253, 244, 0.9)) !important;
}

.landing-page .feature-card:hover {
  box-shadow: 0 12px 30px rgba(37, 99, 235, 0.18);
  transform: translateY(-2px);
}

.landing-page .feature-title {
  margin: var(--space-xs) 0 var(--space-sm);
  font-size: clamp(1rem, 1.5vw, 1.1rem);
  font-weight: 700;
  color: #1d4ed8;
}

.landing-page .feature-desc {
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  line-height: 1.55;
  color: #111827;
  margin: 0;
}

.landing-page .feature-card:nth-child(1) .kicker { color: #15803d; }
.landing-page .feature-card:nth-child(2) .kicker { color: #1d4ed8; }
.landing-page .feature-card:nth-child(3) .kicker { color: #1e3a8a; }

.landing-page .demo-card .inline-code {
  background: var(--landing-blue-soft);
  color: #0369a1;
  border-radius: 6px;
  padding: 2px 6px;
  font-size: 0.9em;
}

.landing-page :deep(.va-card) {
  border-radius: var(--radius-lg);
}

/* Flexible: grids reflow via auto-fit; extra tweaks for very small screens */
@media (max-width: 640px) {
  .landing-page {
    padding: var(--space-md) var(--space-sm) var(--space-lg);
  }
  .landing-page .landing-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-sm);
  }
  .landing-page .landing-tagline {
    white-space: normal;
  }
  .landing-page .hero-main {
    padding: var(--space-md) var(--space-sm) !important;
  }
  .landing-page .portal-card {
    padding: var(--space-md) !important;
  }
  .landing-page .carousel-stage {
    min-height: 250px;
  }
}
</style>
