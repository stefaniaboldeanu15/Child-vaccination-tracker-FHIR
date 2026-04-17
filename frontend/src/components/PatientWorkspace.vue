<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { backendJson, backendFetch, fhirJson } from '@/api/http'
import ResourceSection from '@/components/ResourceSection.vue'
import RecordFormModal from '@/components/RecordFormModal.vue'
import { patientCreateFields, patientUpdateFields, resourceConfigs, type ResourceConfig } from '@/config/resources'

type SessionRole = 'practitioner' | 'related-person'
type Language = 'en' | 'de'

type BundleEntry<T = any> = { resource?: T }
type FhirBundle<T = any> = { entry?: BundleEntry<T>[] }

type PatientSummary = {
  id: string
  fullName: string
  gender?: string | null
  birthDate?: string | null
  svnr?: string | null
}

const props = defineProps<{
  role: SessionRole
  displayName: string
  patientIds?: string[]
  practitionerId?: string | null
  language?: Language
}>()

const i18n = {
  en: {
    practitionerPortal: 'Practitioner portal',
    relatedPersonPortal: 'Related person portal',
    practitionerIntro: 'Search child profiles and manage clinical records in one place.',
    relatedIntro: 'Choose one of your linked children and review the same clinical details in a clean, family-friendly workspace.',
    childProfiles: 'Child profiles',
    immunizations: 'Immunizations',
    linkedProfiles: 'Linked profiles',
    searchPlaceholder: 'Search child by name or SVNR',
    search: 'Search',
    reload: 'Reload',
    addChild: 'Add child',
    loadingPatients: 'Loading patients…',
    noChildRecords: 'No child records found.',
    unknown: 'unknown',
    dob: 'DOB',
    selectedChild: 'Selected child',
    fhirPatientId: 'FHIR Patient ID',
    editChildProfile: 'Edit child profile',
    refreshAll: 'Refresh all',
    resourceNavigation: 'Resource navigation',
    primary: 'Primary',
    secondary: 'Secondary',
    overview: 'Overview',
    relatedPersons: 'Related persons',
    recommendations: 'Recommendations',
    goals: 'Goals',
    appointments: 'Appointments',
    childProfile: 'Child profile',
    selectedChildOverview: 'Selected child overview',
    loadingChildWorkspace: 'Loading child workspace…',
    couldNotLoadWorkspace: 'Could not load the selected child workspace.',
    selectChildHint: 'Select a child from the left side to open the portal workspace.',
    createChildSuccess: 'Child record created successfully.',
    updateChildSuccess: 'Child profile updated successfully.',
    createdSuccessfully: 'created successfully.',
    updatedSuccessfully: 'updated successfully.',
    createChild: 'Create child',
    updateChild: 'Update child',
    editPayload: 'Edit payload',
    cancel: 'Cancel',
    save: 'Save',
  },
  de: {
    practitionerPortal: 'Fachpersonal-Portal',
    relatedPersonPortal: 'Bezugs­personen-Portal',
    practitionerIntro: 'Suchen Sie Kinderprofile und verwalten Sie klinische Daten an einem Ort.',
    relatedIntro: 'Wählen Sie eines Ihrer verknüpften Kinder und prüfen Sie dieselben klinischen Details in einer übersichtlichen Oberfläche.',
    childProfiles: 'Kinderprofile',
    immunizations: 'Impfungen',
    linkedProfiles: 'Verknüpfte Profile',
    searchPlaceholder: 'Kind nach Name oder SVNR suchen',
    search: 'Suchen',
    reload: 'Neu laden',
    addChild: 'Kind hinzufügen',
    loadingPatients: 'Kinder werden geladen…',
    noChildRecords: 'Keine Kinderdatensätze gefunden.',
    unknown: 'unbekannt',
    dob: 'Geburtsdatum',
    selectedChild: 'Ausgewähltes Kind',
    fhirPatientId: 'FHIR Patienten-ID',
    editChildProfile: 'Kinderprofil bearbeiten',
    refreshAll: 'Alles aktualisieren',
    resourceNavigation: 'Ressourcen-Navigation',
    primary: 'Primär',
    secondary: 'Sekundär',
    overview: 'Überblick',
    relatedPersons: 'Bezugs­personen',
    recommendations: 'Empfehlungen',
    goals: 'Ziele',
    appointments: 'Termine',
    childProfile: 'Kinderprofil',
    selectedChildOverview: 'Überblick ausgewähltes Kind',
    loadingChildWorkspace: 'Kind-Arbeitsbereich wird geladen…',
    couldNotLoadWorkspace: 'Der Arbeitsbereich des ausgewählten Kindes konnte nicht geladen werden.',
    selectChildHint: 'Wählen Sie links ein Kind aus, um den Arbeitsbereich zu öffnen.',
    createChildSuccess: 'Kinderdatensatz erfolgreich erstellt.',
    updateChildSuccess: 'Kinderprofil erfolgreich aktualisiert.',
    createdSuccessfully: 'erfolgreich erstellt.',
    updatedSuccessfully: 'erfolgreich aktualisiert.',
    createChild: 'Kind erstellen',
    updateChild: 'Kind aktualisieren',
    editPayload: 'Daten bearbeiten',
    cancel: 'Abbrechen',
    save: 'Speichern',
  },
} as const

const t = computed(() => i18n[props.language ?? 'en'])

const patientQuery = ref('')
const patients = ref<PatientSummary[]>([])
const patientsLoading = ref(false)
const patientsError = ref<string | null>(null)
const selectedPatientId = ref<string | null>(null)
const selectedSection = ref<'overview' | string>('overview')

const patientDetails = ref<Record<string, any> | null>(null)
const patientDetailsLoading = ref(false)
const patientDetailsError = ref<string | null>(null)

const createPatientOpen = ref(false)
const editPatientOpen = ref(false)
const workspaceMessage = ref<string | null>(null)

const resources = reactive<Record<string, { items: Record<string, any>[]; loading: boolean; error: string | null }>>(
    Object.fromEntries(resourceConfigs.map((config) => [config.key, { items: [], loading: false, error: null }])),
)

const linkedChildrenCount = computed(() => props.patientIds?.length ?? 0)
const counts = computed(() =>
    Object.fromEntries(resourceConfigs.map((config) => [config.key, resources[config.key].items.length])),
)
const currentSectionConfig = computed(() =>
    resourceConfigs.find((config) => config.key === selectedSection.value) ?? null,
)
const primaryResourceKeys = ['immunizations', 'recommendations', 'appointments', 'encounters'] as const
const primaryResourceConfigs = computed(() => resourceConfigs.filter((config) => primaryResourceKeys.includes(config.key as any)))
const secondaryResourceConfigs = computed(() => resourceConfigs.filter((config) => !primaryResourceKeys.includes(config.key as any)))

function localizedResourceLabel(config: ResourceConfig) {
  if ((props.language ?? 'en') !== 'de') return config.label
  const deLabels: Record<string, string> = {
    relatedPersons: 'Bezugs­personen',
    immunizations: 'Impfungen',
    recommendations: 'Empfehlungen',
    carePlans: 'Versorgungspläne',
    goals: 'Ziele',
    appointments: 'Termine',
    encounters: 'Begegnungen',
    observations: 'Beobachtungen',
    conditions: 'Diagnosen',
    allergies: 'Allergien',
    consents: 'Einwilligungen',
    communications: 'Mitteilungen',
    adverseEvents: 'Nebenwirkungen',
  }
  return deLabels[config.key] ?? config.label
}

function patientResourceToSummary(resource: any): PatientSummary {
  const name = resource?.name?.[0]
  const given = Array.isArray(name?.given) ? name.given.join(' ') : ''
  const family = name?.family ?? ''

  return {
    id: resource?.id ?? '',
    fullName: [given, family].filter(Boolean).join(' ') || `Patient/${resource?.id ?? 'unknown'}`,
    gender: resource?.gender ?? null,
    birthDate: resource?.birthDate ?? null,
    svnr: findIdentifier(resource?.identifier, 'app:svnr'),
  }
}

function findIdentifier(identifiers: any[] | undefined, system: string) {
  return (identifiers ?? []).find((identifier) => identifier?.system === system)?.value ?? null
}

function formatDate(value?: string | null) {
  if (!value) return '—'
  return new Date(value).toLocaleDateString()
}

function toRelatedPersonPath(path: string) {
  return path.replace('/api/practitioner/', '/api/related-person/')
}

async function backendJsonForRole<T>(path: string): Promise<T> {
  if (props.role !== 'related-person') {
    return backendJson<T>(path)
  }

  const relatedPath = toRelatedPersonPath(path)
  try {
    return await backendJson<T>(relatedPath)
  } catch {
    return backendJson<T>(path)
  }
}

async function backendFetchForRole(path: string, options: RequestInit) {
  if (props.role !== 'related-person') {
    return backendFetch(path, options)
  }

  const relatedPath = toRelatedPersonPath(path)
  try {
    return await backendFetch(relatedPath, options)
  } catch {
    return backendFetch(path, options)
  }
}

async function fetchPatientSummary(patientId: string) {
  const resource = await fhirJson<any>(`/fhir/Patient/${patientId}`)
  return patientResourceToSummary(resource)
}

async function loadPatients() {
  patientsLoading.value = true
  patientsError.value = null

  try {
    if (props.role === 'related-person') {
      const ids = props.patientIds ?? []
      const result = await Promise.all(ids.map((id) => fetchPatientSummary(id)))
      patients.value = result.filter((item): item is PatientSummary => !!item && !!item.id)
    } else {
      const query = patientQuery.value.trim()
      let bundle: FhirBundle

      if (!query) {
        bundle = await fhirJson<FhirBundle>('/fhir/Patient?_count=25')
      } else {
        bundle = await fhirJson<FhirBundle>(`/fhir/Patient?name=${encodeURIComponent(query)}`)
        if ((bundle.entry ?? []).length === 0) {
          bundle = await fhirJson<FhirBundle>(`/fhir/Patient?identifier=${encodeURIComponent(query)}`)
        }
      }

      patients.value = (bundle.entry ?? [])
          .map((entry) => entry.resource)
          .filter(Boolean)
          .map((resource: any) => patientResourceToSummary(resource))
    }

    if (patients.value.length === 0) {
      selectedPatientId.value = null
      patientDetails.value = null
      return
    }

    const currentExists = !!selectedPatientId.value && patients.value.some((patient) => patient.id === selectedPatientId.value)
    if (!currentExists) {
      selectedPatientId.value = patients.value[0].id
    }
  } catch (error) {
    patients.value = []
    selectedPatientId.value = null
    patientDetails.value = null
    patientsError.value = String(error)
  } finally {
    patientsLoading.value = false
  }
}

async function loadResource(config: ResourceConfig) {
  if (!selectedPatientId.value) return

  resources[config.key].loading = true
  resources[config.key].error = null

  try {
    const bundle = await searchBundle(config.searchPath(selectedPatientId.value))
    const ids = (bundle.entry ?? [])
        .map((entry) => entry.resource?.id as string | undefined)
        .filter(Boolean) as string[]

    const items = await Promise.all(ids.map((id) => backendJsonForRole<Record<string, any>>(config.getPath(id))))
    resources[config.key].items = items
  } catch (error) {
    resources[config.key].items = []
    resources[config.key].error = String(error)
  } finally {
    resources[config.key].loading = false
  }
}

async function searchBundle(pathOrPaths: string | string[]) {
  const candidates = Array.isArray(pathOrPaths) ? pathOrPaths : [pathOrPaths]
  let lastError: unknown = null

  for (const candidate of candidates) {
    try {
      return await fhirJson<FhirBundle>(candidate)
    } catch (error) {
      lastError = error
    }
  }

  throw lastError ?? new Error('Resource search failed')
}

async function loadSelectedPatient() {
  if (!selectedPatientId.value) return

  patientDetailsLoading.value = true
  patientDetailsError.value = null
  workspaceMessage.value = null

  try {
    patientDetails.value = await backendJsonForRole<Record<string, any>>(`/api/practitioner/patients/${selectedPatientId.value}`)

    for (const config of resourceConfigs) {
      await loadResource(config)
    }
  } catch (error) {
    patientDetails.value = null
    patientDetailsError.value = String(error)
  } finally {
    patientDetailsLoading.value = false
  }
}

async function openPatient(patientId: string) {
  selectedPatientId.value = patientId
  selectedSection.value = 'overview'
  await loadSelectedPatient()
}

async function createPatient(payload: Record<string, any>) {
  await backendFetch('/api/practitioner/patients', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  createPatientOpen.value = false
  workspaceMessage.value = t.value.createChildSuccess
  patientQuery.value = ''
  await loadPatients()

  if (selectedPatientId.value) {
    await loadSelectedPatient()
  }
}

async function updatePatient(payload: Record<string, any>) {
  if (!selectedPatientId.value) return

  await backendFetchForRole(`/api/practitioner/patients/${selectedPatientId.value}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(payload),
  })

  editPatientOpen.value = false
  workspaceMessage.value = t.value.updateChildSuccess
  await loadSelectedPatient()
  await loadPatients()
}

async function createResource(config: ResourceConfig, payload: Record<string, any>) {
  if (!selectedPatientId.value || !config.createPath) return

  const path = typeof config.createPath === 'function' ? config.createPath(selectedPatientId.value) : config.createPath
  const finalPayload = enrichPayload(config.key, payload, selectedPatientId.value)

  await backendFetchForRole(path, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(finalPayload),
  })

  workspaceMessage.value = `${localizedResourceLabel(config)} ${t.value.createdSuccessfully}`
  await loadResource(config)
}

async function updateResource(config: ResourceConfig, id: string, payload: Record<string, any>) {
  if (!config.updatePath) return

  await backendFetchForRole(config.updatePath(id), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(enrichPayload(config.key, payload, selectedPatientId.value)),
  })

  workspaceMessage.value = `${localizedResourceLabel(config)} ${t.value.updatedSuccessfully}`
  await loadResource(config)
}

function enrichPayload(key: string, payload: Record<string, any>, patientId: string | null) {
  if (!patientId || payload.patientId) return payload

  const needsPatientId = new Set([
    'immunizations',
    'recommendations',
    'carePlans',
    'goals',
    'appointments',
    'allergies',
    'consents',
    'communications',
    'adverseEvents',
  ])

  if (!needsPatientId.has(key)) return payload
  return { patientId, ...payload }
}

onMounted(async () => {
  await loadPatients()
  if (selectedPatientId.value) {
    await loadSelectedPatient()
  }
})
</script>

<template>
  <div class="workspace-shell theme-official">
    <div class="workspace-grid">
      <va-card class="surface-card sidebar-card">
        <div class="section-header card-header">
          <div>
            <div class="kicker">{{ role === 'practitioner' ? t.practitionerPortal : t.relatedPersonPortal }}</div>
            <h2 class="card-title">{{ displayName }}</h2>
          </div>
        </div>

        <p class="intro-text">
          <template v-if="role === 'practitioner'">
            {{ t.practitionerIntro }}
          </template>
          <template v-else>
            {{ t.relatedIntro }}
          </template>
        </p>

        <div class="metric-grid sidebar-metrics">
          <div class="metric">
            <div class="metric-value">{{ patients.length }}</div>
            <div class="metric-label">{{ t.childProfiles }}</div>
          </div>
          <div class="metric">
            <div class="metric-value">{{ counts.immunizations ?? 0 }}</div>
            <div class="metric-label">{{ t.immunizations }}</div>
          </div>
          <div class="metric">
            <div class="metric-value">{{ linkedChildrenCount }}</div>
            <div class="metric-label">{{ t.linkedProfiles }}</div>
          </div>
        </div>

        <div class="toolbar sidebar-actions">
          <va-input
              v-if="role === 'practitioner'"
              v-model="patientQuery"
              class="patient-search"
              :placeholder="t.searchPlaceholder"
              @keyup.enter="loadPatients"
          />
          <div class="sidebar-actions-row">
            <va-button @click="loadPatients">
              {{ role === 'practitioner' ? t.search : t.reload }}
            </va-button>
            <va-button
                v-if="role === 'practitioner'"
                preset="secondary"
                @click="createPatientOpen = true"
            >
              {{ t.addChild }}
            </va-button>
          </div>
        </div>

        <va-alert v-if="patientsError" color="danger" outline class="alert-box">{{ patientsError }}</va-alert>

        <div v-if="patientsLoading" class="empty-state">{{ t.loadingPatients }}</div>
        <div v-else-if="patients.length === 0" class="empty-state">{{ t.noChildRecords }}</div>
        <div v-else class="patient-list">
          <button
              v-for="patient in patients"
              :key="patient.id"
              class="patient-tile"
              :class="{ active: patient.id === selectedPatientId }"
              @click="openPatient(patient.id)"
          >
            <div class="patient-tile-top">
              <div>
                <strong class="patient-name">{{ patient.fullName }}</strong>
                <div class="patient-id">{{ patient.id }}</div>
              </div>
              <va-badge :text="patient.gender || t.unknown" color="primary" />
            </div>
            <div class="patient-meta">
              <span v-if="patient.birthDate">{{ t.dob }}: {{ formatDate(patient.birthDate) }}</span>
              <span v-if="patient.svnr"> · SVNR: {{ patient.svnr }}</span>
            </div>
          </button>
        </div>
      </va-card>

      <va-card class="surface-card workspace-card">
        <div v-if="patientDetailsLoading" class="empty-state">{{ t.loadingChildWorkspace }}</div>

        <template v-else-if="selectedPatientId && patientDetails">
        <div class="workspace-header">
            <div>
              <div class="kicker">{{ t.selectedChild }}</div>
              <h1 class="workspace-title">{{ patientDetails.fullName || selectedPatientId }}</h1>
              <div class="workspace-meta">
                <span>{{ t.fhirPatientId }}: <code class="inline-code">{{ patientDetails.id || selectedPatientId }}</code></span>
                <span v-if="patientDetails.svnr"> · SVNR: {{ patientDetails.svnr }}</span>
              </div>
            </div>
          <div class="toolbar workspace-actions">
              <va-button
                  v-if="role === 'practitioner'"
                preset="primary"
                  @click="editPatientOpen = true"
              >
                {{ t.editChildProfile }}
              </va-button>
              <button class="refresh-link" type="button" @click="loadSelectedPatient">{{ t.refreshAll }}</button>
            </div>
          </div>

          <va-alert v-if="workspaceMessage" color="success" outline class="alert-box">
            {{ workspaceMessage }}
          </va-alert>
          <va-alert v-if="patientDetailsError" color="danger" outline class="alert-box">
            {{ patientDetailsError }}
          </va-alert>

          <div class="resource-nav-wrap">
            <div class="resource-nav-title">{{ t.resourceNavigation }}</div>
            <div class="resource-row-label">{{ t.primary }}</div>
            <div class="scroll-tabs">
              <button
                  class="tab-chip"
                  :class="{ active: selectedSection === 'overview' }"
                  @click="selectedSection = 'overview'"
              >
                {{ t.overview }}
              </button>
              <button
                  v-for="config in primaryResourceConfigs"
                  :key="config.key"
                  class="tab-chip"
                  :class="{ active: selectedSection === config.key }"
                  @click="selectedSection = config.key"
              >
                {{ localizedResourceLabel(config) }}
              </button>
            </div>
            <div class="resource-row-label secondary">{{ t.secondary }}</div>
            <div class="scroll-tabs secondary-tabs">
              <button
                  v-for="config in secondaryResourceConfigs"
                  :key="config.key"
                  class="tab-chip tab-chip-secondary"
                  :class="{ active: selectedSection === config.key }"
                  @click="selectedSection = config.key"
              >
                {{ localizedResourceLabel(config) }}
              </button>
            </div>
          </div>

          <section v-if="selectedSection === 'overview'" class="section-stack">
            <div class="metric-grid overview-grid">
              <div class="metric">
                <div class="metric-value">{{ counts.relatedPersons ?? 0 }}</div>
                <div class="metric-label">{{ t.relatedPersons }}</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ counts.recommendations ?? 0 }}</div>
                <div class="metric-label">{{ t.recommendations }}</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ counts.goals ?? 0 }}</div>
                <div class="metric-label">{{ t.goals }}</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ counts.appointments ?? 0 }}</div>
                <div class="metric-label">{{ t.appointments }}</div>
              </div>
            </div>

            <article class="record-card">
              <div class="kicker">{{ t.childProfile }}</div>
              <h2 class="record-title">{{ t.selectedChildOverview }}</h2>
              <div class="record-grid">
                <div v-for="(value, key) in patientDetails" :key="key" class="record-item">
                  <strong>{{ key }}</strong>
                  <span>{{ value || '—' }}</span>
                </div>
              </div>
            </article>
          </section>

          <ResourceSection
              v-else-if="currentSectionConfig"
              :config="currentSectionConfig"
              :items="resources[currentSectionConfig.key].items"
              :loading="resources[currentSectionConfig.key].loading"
              :error="resources[currentSectionConfig.key].error"
              :can-create="role === 'practitioner' || role === 'related-person'"
              :can-edit="role === 'practitioner' || role === 'related-person'"
              @create="createResource"
              @update="updateResource"
          />
        </template>

        <div v-else-if="selectedPatientId && patientDetailsError" class="empty-state error-state">
          <div>
            <strong>{{ t.couldNotLoadWorkspace }}</strong>
            <div class="error-text">{{ patientDetailsError }}</div>
          </div>
        </div>

        <div v-else class="empty-state">{{ t.selectChildHint }}</div>
      </va-card>
    </div>
  </div>

  <RecordFormModal
      v-model="createPatientOpen"
      :title="t.createChild"
      :kicker-label="t.editPayload"
      :cancel-label="t.cancel"
      :save-label="t.save"
      :fields="patientCreateFields"
      @save="createPatient"
  />

  <RecordFormModal
      v-if="patientDetails && role === 'practitioner'"
      v-model="editPatientOpen"
      :title="t.updateChild"
      :kicker-label="t.editPayload"
      :cancel-label="t.cancel"
      :save-label="t.save"
      :fields="patientUpdateFields"
      :initial-value="patientDetails"
      @save="updatePatient"
  />
</template>

<style scoped>
.workspace-shell {
  --surface-border: rgba(15, 57, 141, 0.2);
  --surface-shadow: rgba(15, 57, 141, 0.12);
  --surface-bg-end: rgba(238, 246, 255, 0.98);
  --sidebar-bg-end: rgba(232, 248, 241, 0.92);
  --workspace-bg-end: rgba(231, 242, 255, 0.94);
  --kicker-color: #0a7ea8;
  --muted-text: #3f5878;
  --metric-bg-start: rgba(235, 247, 255, 0.95);
  --metric-bg-end: rgba(230, 246, 238, 0.9);
  --metric-border: rgba(15, 57, 141, 0.16);
  --nav-bg-start: rgba(235, 246, 255, 0.9);
  --nav-bg-end: rgba(232, 248, 241, 0.86);
  --record-item-start: rgba(233, 246, 255, 0.94);
  --record-item-end: rgba(232, 248, 241, 0.9);
  --record-item-border: rgba(10, 126, 168, 0.2);
}

.workspace-shell.theme-soft {
  --surface-border: rgba(21, 76, 84, 0.16);
  --surface-shadow: rgba(21, 76, 84, 0.1);
  --surface-bg-end: rgba(245, 252, 249, 0.98);
  --sidebar-bg-end: rgba(235, 248, 239, 0.9);
  --workspace-bg-end: rgba(231, 246, 238, 0.9);
  --kicker-color: #188d77;
  --muted-text: #4a6366;
  --metric-bg-start: rgba(237, 249, 241, 0.95);
  --metric-bg-end: rgba(233, 246, 239, 0.9);
  --metric-border: rgba(21, 76, 84, 0.14);
  --nav-bg-start: rgba(236, 249, 242, 0.9);
  --nav-bg-end: rgba(232, 246, 238, 0.86);
  --record-item-start: rgba(236, 249, 242, 0.92);
  --record-item-end: rgba(230, 245, 236, 0.88);
  --record-item-border: rgba(24, 141, 119, 0.2);
}

.workspace-shell {
  width: calc(100vw - 40px);
  max-width: none;
  margin-left: calc(50% - 50vw + 20px);
  margin-right: calc(50% - 50vw + 20px);
}

.workspace-grid {
  display: grid;
  grid-template-columns: 390px minmax(0, 1fr);
  gap: 24px;
  align-items: start;
}

.surface-card {
  border-radius: 30px;
  border: 1px solid var(--surface-border);
  box-shadow: 0 20px 46px var(--surface-shadow);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, var(--surface-bg-end) 100%);
}

.sidebar-card,
.workspace-card {
  padding: 24px;
}

.sidebar-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, var(--sidebar-bg-end) 100%);
}

.workspace-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, var(--workspace-bg-end) 100%);
  min-height: 640px;
}

.card-header,
.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 16px;
  margin-bottom: 14px;
}

.card-title,
.record-title {
  margin: 4px 0 0;
  color: #0b2d72;
  font-size: 1.55rem;
  line-height: 1.18;
}

.workspace-title {
  margin: 4px 0 8px;
  color: #0b2d72;
  font-size: 2rem;
  line-height: 1.14;
}

.kicker {
  color: var(--kicker-color);
  font-weight: 700;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  font-size: 0.76rem;
}

.intro-text,
.workspace-meta,
.patient-id,
.patient-meta {
  color: var(--muted-text);
}

.intro-text {
  margin: 0 0 14px;
  font-size: 0.95rem;
  line-height: 1.58;
}

.metric-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 12px;
  margin-bottom: 14px;
}

.overview-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
}

.metric {
  background: linear-gradient(180deg, var(--metric-bg-start) 0%, var(--metric-bg-end) 100%);
  border: 1px solid var(--metric-border);
  border-radius: 18px;
  padding: 14px 16px;
  min-height: 96px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.metric-value {
  color: #0b2d72;
  font-size: 1.6rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 6px;
}

.metric-label {
  color: var(--muted-text);
  font-size: 0.86rem;
  line-height: 1.3;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  align-items: center;
}

.patient-search {
  flex: 1 1 260px;
  min-width: 260px;
}

.sidebar-actions {
  margin-bottom: 14px;
  align-items: stretch;
}

.sidebar-actions-row {
  display: flex;
  gap: 10px;
  align-items: center;
  flex-wrap: wrap;
}

.alert-box {
  margin-bottom: 16px;
  border-radius: 16px;
}

.patient-list {
  display: grid;
  gap: 12px;
}

.patient-tile {
  width: 100%;
  text-align: left;
  background: #ffffff;
  border: 1px solid rgba(9, 146, 194, 0.22);
  border-radius: 18px;
  padding: 14px 14px 12px;
  transition: 0.2s ease;
  cursor: pointer;
}

.patient-tile:hover {
  border-color: rgba(9, 146, 194, 0.46);
  box-shadow: 0 10px 24px rgba(9, 146, 194, 0.12);
  transform: translateY(-1px);
}

.patient-tile.active {
  border-color: #0992c2;
  background: linear-gradient(180deg, rgba(9, 146, 194, 0.08) 0%, rgba(74, 188, 118, 0.08) 100%);
  box-shadow: 0 12px 28px rgba(9, 146, 194, 0.14);
}

.patient-tile-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.patient-name {
  display: block;
  font-size: 1.02rem;
  line-height: 1.25;
  color: #0b2d72;
}

.patient-id {
  margin-top: 2px;
  font-size: 0.82rem;
}

.patient-meta {
  margin-top: 8px;
  font-size: 0.82rem;
}

.resource-nav-wrap {
  border: 1px solid rgba(11, 45, 114, 0.13);
  border-radius: 16px;
  padding: 12px;
  background: linear-gradient(180deg, var(--nav-bg-start) 0%, var(--nav-bg-end) 100%);
  margin: 6px 0 16px;
}

.resource-nav-title {
  font-size: 0.74rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  color: #4b6588;
  margin-bottom: 10px;
}

.resource-row-label {
  font-size: 0.7rem;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-weight: 700;
  color: #4b6588;
  margin: 2px 0 8px;
}

.resource-row-label.secondary {
  margin-top: 12px;
}

.scroll-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.secondary-tabs {
  margin-top: 2px;
}

.tab-chip {
  border: 1px solid rgba(11, 45, 114, 0.15);
  background: #ffffff;
  color: #173458;
  border-radius: 999px;
  padding: 7px 12px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: 0.16s ease;
}

.tab-chip:hover {
  border-color: rgba(9, 146, 194, 0.38);
  background: linear-gradient(180deg, rgba(9, 146, 194, 0.08) 0%, rgba(74, 188, 118, 0.08) 100%);
}

.tab-chip.active {
  background: #123f9d;
  color: #fff;
  border-color: #123f9d;
  box-shadow: 0 8px 16px rgba(18, 63, 157, 0.24);
}

.tab-chip-secondary {
  color: #3c4f70;
  border-color: rgba(11, 45, 114, 0.12);
}

.section-stack {
  display: grid;
  gap: 16px;
}

.record-card {
  background: #ffffff;
  border: 1px solid rgba(11, 45, 114, 0.12);
  border-radius: 18px;
  padding: 18px;
  box-shadow: 0 12px 26px rgba(11, 45, 114, 0.05);
}

.record-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 12px;
}

.record-item {
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: linear-gradient(180deg, var(--record-item-start) 0%, var(--record-item-end) 100%);
  border: 1px solid var(--record-item-border);
  border-radius: 14px;
  padding: 10px;
  min-height: 68px;
}

.record-item strong {
  color: #0b2d72;
  font-size: 0.82rem;
  text-transform: capitalize;
}

.record-item span {
  color: #1a3b62;
  font-size: 0.9rem;
  line-height: 1.35;
  word-break: break-word;
}

.inline-code {
  background: rgba(228, 241, 255, 0.92);
  color: #0b2d72;
  border-radius: 10px;
  padding: 3px 8px;
  font-size: 0.95rem;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 180px;
  border: 1px dashed rgba(11, 45, 114, 0.2);
  border-radius: 22px;
  color: var(--muted-text);
  font-size: 0.93rem;
  background: rgba(255, 255, 255, 0.62);
  text-align: center;
  padding: 24px;
}

.error-state {
  color: #0b2d72;
}

.error-text {
  margin-top: 8px;
  white-space: pre-wrap;
  word-break: break-word;
}

:deep(.va-input-wrapper),
:deep(.va-input__container) {
  min-height: 52px;
  border-radius: 16px;
}

:deep(.va-input) {
  width: 100%;
}

:deep(.va-button) {
  min-height: 42px;
  padding: 0 16px;
  border-radius: 12px;
  font-weight: 700;
  letter-spacing: 0;
}

.workspace-actions :deep(.va-button--primary) {
  box-shadow: 0 8px 18px rgba(11, 45, 114, 0.19);
}

.refresh-link {
  background: transparent;
  border: none;
  color: var(--muted-text);
  font-size: 0.84rem;
  font-weight: 600;
  padding: 4px 0;
  cursor: pointer;
  text-decoration: underline;
  text-underline-offset: 2px;
}

.refresh-link:hover {
  color: #0b2d72;
}

:deep(.va-button--primary) {
  background: #123f9d !important;
  border-color: #123f9d !important;
  color: #ffffff !important;
}

:deep(.va-button--primary:hover) {
  background: #0f367f !important;
  border-color: #0f367f !important;
}

:deep(.va-button--secondary) {
  background: rgba(10, 126, 168, 0.1) !important;
  border-color: rgba(10, 126, 168, 0.26) !important;
  color: #0f367f !important;
}

:deep(.va-button--secondary:hover) {
  background: rgba(10, 126, 168, 0.16) !important;
  border-color: rgba(10, 126, 168, 0.34) !important;
}

:deep(.va-badge__text) {
  font-weight: 700;
  letter-spacing: 0.02em;
}

:deep(.va-card__content) {
  padding: 0;
}

@media (max-width: 1600px) {
  .workspace-grid {
    grid-template-columns: 350px minmax(0, 1fr);
  }
}

@media (max-width: 1400px) {
  .workspace-shell {
    width: calc(100vw - 28px);
    margin-left: calc(50% - 50vw + 14px);
    margin-right: calc(50% - 50vw + 14px);
  }

  .record-grid,
  .overview-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 1180px) {
  .workspace-grid {
    grid-template-columns: 1fr;
  }

  .workspace-title {
    font-size: 1.65rem;
  }
}

@media (max-width: 768px) {
  .workspace-shell {
    width: calc(100vw - 20px);
    margin-left: calc(50% - 50vw + 10px);
    margin-right: calc(50% - 50vw + 10px);
  }

  .sidebar-card,
  .workspace-card {
    padding: 20px;
  }

  .record-grid,
  .overview-grid {
    grid-template-columns: 1fr;
  }

  .workspace-title {
    font-size: 1.35rem;
  }

  .patient-tile-top,
  .workspace-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
