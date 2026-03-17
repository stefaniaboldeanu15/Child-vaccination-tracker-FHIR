<script setup lang="ts">
import VaccinationLogo from '@/components/VaccinationLogo.vue'
import AuthPortalCard from '@/components/AuthPortalCard.vue'
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
          <VaccinationLogo variant="icon" class="logo-icon logo-icon-fluid" />
          <div class="header-text">
            <h1 class="landing-title">VaxCare Junior</h1>
            <p class="landing-tagline">Pediatric vaccination records, schedules, and care updates in one secure place.
            </p>
          </div>
        </header>

        <div class="hero-copy">
          <p class="hero-actions-label">Choose your portal, then either sign in or create an account.</p>
        </div>

        <div class="portals-grid landing-auth-grid">
          <AuthPortalCard role="practitioner" badge="For clinicians" title="Practitioner portal"
            subtitle="Manage patient records and keep immunizations up to date"
            description="Look up children, view full vaccination history, see what’s due, and record new immunizations in one workspace tied to your practice." />
          <AuthPortalCard role="related-person" badge="For parents & guardians" title="Family portal"
            subtitle="One place for all your children’s vaccination records"
            description="Sign in once to see the child linked to your account, check what’s due, and keep your family’s vaccinations on track." />
        </div>
      </va-card>
    </section>

    <!-- What you can do -->
    <section class="landing-section features-section">
      <h2 class="section-title">What you can do</h2>
      <div class="features-grid">
        <va-card class="surface-card hero-card feature-card">
          <div class="kicker">Stay on schedule</div>
          <h3 class="feature-title">See what’s due</h3>
          <p class="feature-desc">
            View vaccines due for each child at a glance. Update status and due dates with your clinician so the whole
            family stays on schedule.
          </p>
        </va-card>
        <va-card class="surface-card hero-card feature-card">
          <div class="kicker">Full picture</div>
          <h3 class="feature-title">Complete visit history</h3>
          <p class="feature-desc">
            Each visit is shown with immunizations given and notes in one place—so you and your care team always have
            the full story.
          </p>
        </va-card>
        <va-card class="surface-card hero-card feature-card demo-card">
          <div class="kicker">Try it out</div>
          <h3 class="feature-title">Demo access</h3>
          <p class="feature-desc">
            Practitioner: <code class="inline-code">dr.mueller</code> / <code
              class="inline-code">pwMueller01</code><br />
            Parent: <code class="inline-code">anna.gruber.parent</code> / <code class="inline-code">Parent123!</code>
          </p>
        </va-card>
      </div>
    </section>
  </main>
</template>

<style scoped>
.landing-page .hero-main {
  display: grid;
  gap: var(--space-lg);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.92), rgba(255, 255, 255, 0.86));
}

.landing-page {
  --landing-accent: #0d9488;
  --landing-accent-soft: rgba(13, 148, 136, 0.12);
  --landing-blue: #0284c7;
  --landing-blue-soft: rgba(2, 132, 199, 0.1);
  --landing-teal: #0f766e;
  --landing-mint: #059669;
  --landing-violet: #6d28d9;
  --landing-amber: #d97706;
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
  --text-secondary: #334155;
  --text-muted: #475569;
  --btn-accent: #0d9488;
  --btn-accent-hover: #0f766e;
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
    radial-gradient(ellipse 100% 60% at 50% -10%, rgba(13, 148, 136, 0.06), transparent 50%),
    radial-gradient(ellipse 70% 50% at 100% 20%, rgba(2, 132, 199, 0.04), transparent 45%),
    radial-gradient(ellipse 60% 40% at 0% 60%, rgba(5, 150, 105, 0.04), transparent 45%);
  pointer-events: none;
  z-index: 0;
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
  color: var(--text-primary);
  margin: 0 0 var(--space-xs);
}

.landing-page .portal-subtitle {
  font-size: clamp(0.9rem, 1.5vw, 1rem);
  font-weight: 600;
  color: var(--text-secondary);
  margin: 0 0 var(--space-sm);
}

.landing-page .portal-desc {
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  line-height: 1.55;
  color: var(--text-secondary);
  margin: 0;
}

.landing-page .features-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(min(260px, 100%), 1fr));
  gap: var(--space-md);
}

.landing-page .hero-main {
  padding: var(--space-md) var(--space-md) var(--space-lg) !important;
  background: var(--glass);
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
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
}

.landing-page .landing-header .logo-icon {
  --logo-color: #0d9488;
  flex-shrink: 0;
}

.landing-page .landing-header .logo-icon-fluid {
  width: clamp(48px, 10vw, 64px);
  height: clamp(48px, 10vw, 64px);
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
  font-size: clamp(1rem, 1.5vw, 1.05rem);
  line-height: 1.5;
  margin: 0;
  max-width: 42ch;
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

.landing-page .hero-actions-label {
  font-size: 1rem;
  font-weight: 600;
  letter-spacing: 0.04em;
  color: var(--text-muted);
  margin: 0 0 var(--space-sm);
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
  border: 1px solid var(--glass-border);
  background: var(--glass);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: var(--shadow-soft);
  transition: transform 0.35s var(--ease-spring), box-shadow 0.35s var(--ease-out);
}

.landing-page .metric:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.06);
}

.landing-page .metric .value {
  font-size: clamp(1.25rem, 2vw, 1.5rem);
  font-weight: 800;
  color: var(--text-primary);
  letter-spacing: -0.02em;
}

.landing-page .metric .label {
  font-size: clamp(0.8rem, 1.2vw, 0.9rem);
  color: var(--text-secondary);
  margin-top: var(--space-xs);
}

.landing-page .metric-smart {
  border-color: rgba(2, 132, 199, 0.2);
  background: rgba(255, 255, 255, 0.7) !important;
}

.landing-page .metric-smart .value {
  color: var(--landing-blue);
}

.landing-page .metric-fhir {
  border-color: rgba(13, 148, 136, 0.2);
  background: rgba(255, 255, 255, 0.7) !important;
}

.landing-page .metric-fhir .value {
  color: var(--landing-teal);
}

.landing-page .metric-vuestic {
  border-color: rgba(109, 40, 217, 0.2);
  background: rgba(255, 255, 255, 0.7) !important;
}

.landing-page .metric-vuestic .value {
  color: var(--landing-violet);
}

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

.landing-page :deep(.va-button.btn-parent) {
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

.landing-page :deep(.va-button.btn-parent .btn-icon) {
  color: var(--btn-accent);
}

.landing-page :deep(.va-button.btn-parent:hover) {
  background: rgba(13, 148, 136, 0.1) !important;
  border-color: var(--btn-accent-hover) !important;
  color: var(--btn-accent-hover) !important;
  transform: translateY(-1px) !important;
}

.landing-page :deep(.va-button.btn-parent:hover .btn-icon) {
  color: var(--btn-accent-hover);
}

.landing-page .portal-practitioner {
  border-left: 5px solid var(--landing-blue);
  background: var(--glass) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.landing-page .portal-parent {
  border-left: 5px solid var(--landing-mint);
  background: var(--glass) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
}

.landing-page .portal-card {
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  padding: var(--space-md) !important;
  min-width: 0;
  transition: box-shadow 0.4s var(--ease-out), transform 0.4s var(--ease-spring);
}

.landing-page .portal-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-3px);
}


.landing-page .feature-card {
  border: 1px solid var(--glass-border);
  border-radius: var(--radius-lg);
  box-shadow: var(--shadow-soft);
  background: var(--glass) !important;
  backdrop-filter: blur(20px);
  -webkit-backdrop-filter: blur(20px);
  min-width: 0;
  transition: box-shadow 0.4s var(--ease-out), transform 0.4s var(--ease-spring);
}

.landing-page .feature-card:nth-child(1) {
  border-left: 4px solid var(--landing-amber);
}

.landing-page .feature-card:nth-child(2) {
  border-left: 4px solid var(--landing-violet);
}

.landing-page .feature-card:nth-child(3) {
  border-left: 4px solid var(--landing-blue);
}

.landing-page .feature-card:hover {
  box-shadow: var(--shadow-hover);
  transform: translateY(-2px);
}

.landing-page .feature-title {
  margin: var(--space-xs) 0 var(--space-sm);
  font-size: clamp(1rem, 1.5vw, 1.1rem);
  font-weight: 700;
  color: var(--text-primary);
}

.landing-page .feature-desc {
  font-size: clamp(0.9rem, 1.2vw, 1rem);
  line-height: 1.55;
  color: var(--text-secondary);
  margin: 0;
}

.landing-page .feature-card:nth-child(1) .kicker {
  color: var(--landing-amber);
}

.landing-page .feature-card:nth-child(2) .kicker {
  color: var(--landing-violet);
}

.landing-page .feature-card:nth-child(3) .kicker {
  color: var(--landing-blue);
}

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

.needle-icon:nth-child(1) {
  left: 5%;
  top: 8%;
  animation-delay: 0s;
}

.needle-icon:nth-child(2) {
  left: 18%;
  top: 15%;
  animation-delay: -1.5s;
  transform: rotate(-12deg);
}

.needle-icon:nth-child(3) {
  left: 28%;
  top: 6%;
  animation-delay: -3s;
  transform: rotate(8deg);
}

.needle-icon:nth-child(4) {
  left: 42%;
  top: 12%;
  animation-delay: -0.5s;
  transform: rotate(-5deg);
}

.needle-icon:nth-child(5) {
  left: 55%;
  top: 5%;
  animation-delay: -2s;
}

.needle-icon:nth-child(6) {
  left: 68%;
  top: 14%;
  animation-delay: -4s;
  transform: rotate(10deg);
}

.needle-icon:nth-child(7) {
  left: 82%;
  top: 9%;
  animation-delay: -1s;
  transform: rotate(-8deg);
}

.needle-icon:nth-child(8) {
  left: 92%;
  top: 18%;
  animation-delay: -2.5s;
}

.needle-icon:nth-child(9) {
  left: 8%;
  top: 35%;
  animation-delay: -3.5s;
  transform: rotate(6deg);
}

.needle-icon:nth-child(10) {
  left: 22%;
  top: 42%;
  animation-delay: -0.8s;
  transform: rotate(-10deg);
}

.needle-icon:nth-child(11) {
  left: 75%;
  top: 38%;
  animation-delay: -2.2s;
  transform: rotate(4deg);
}

.needle-icon:nth-child(12) {
  left: 88%;
  top: 45%;
  animation-delay: -4.5s;
  transform: rotate(-6deg);
}

.needle-icon:nth-child(13) {
  left: 12%;
  top: 62%;
  animation-delay: -1.2s;
  transform: rotate(12deg);
}

.needle-icon:nth-child(14) {
  left: 35%;
  top: 68%;
  animation-delay: -3.2s;
  transform: rotate(-4deg);
}

.needle-icon:nth-child(15) {
  left: 58%;
  top: 72%;
  animation-delay: -0.3s;
  transform: rotate(7deg);
}

.needle-icon:nth-child(16) {
  left: 78%;
  top: 65%;
  animation-delay: -2.8s;
  transform: rotate(-9deg);
}

.needle-icon:nth-child(17) {
  left: 5%;
  top: 82%;
  animation-delay: -4.2s;
  transform: rotate(5deg);
}

.needle-icon:nth-child(18) {
  left: 45%;
  top: 88%;
  animation-delay: -1.8s;
  transform: rotate(-7deg);
}

.needle-icon:nth-child(19) {
  left: 65%;
  top: 92%;
  animation-delay: -3.8s;
  transform: rotate(3deg);
}

.needle-icon:nth-child(20) {
  left: 90%;
  top: 78%;
  animation-delay: -0.6s;
  transform: rotate(-11deg);
}

.needle-icon:nth-child(21) {
  left: 50%;
  top: 28%;
  animation-delay: -2.3s;
  transform: rotate(9deg);
  opacity: 0.08;
}

.needle-icon:nth-child(22) {
  left: 30%;
  top: 55%;
  animation-delay: -4s;
  transform: rotate(-3deg);
  opacity: 0.08;
}

.needle-icon:nth-child(23) {
  left: 70%;
  top: 52%;
  animation-delay: -1.4s;
  transform: rotate(6deg);
  opacity: 0.08;
}

.needle-icon:nth-child(24) {
  left: 15%;
  top: 78%;
  animation-delay: -3.3s;
  transform: rotate(-8deg);
  opacity: 0.08;
}

@keyframes needle-float {

  0%,
  100% {
    opacity: 0.05;
  }

  50% {
    opacity: 0.1;
  }
}

@media (max-width: 960px) {

  .landing-page .portals-grid,
  .landing-page .features-grid {
    grid-template-columns: 1fr;
  }

  .landing-page .landing-header {
    align-items: flex-start;
  }
}

@media (max-width: 640px) {
  .landing-page {
    padding: var(--space-md) var(--space-sm) var(--space-lg);
  }

  .landing-page .landing-header {
    flex-direction: column;
    align-items: flex-start;
    gap: var(--space-sm);
  }

  .landing-page .hero-main {
    padding: var(--space-md) var(--space-sm) !important;
  }

  .landing-page .portal-card {
    padding: var(--space-md) !important;
  }
}
</style>
