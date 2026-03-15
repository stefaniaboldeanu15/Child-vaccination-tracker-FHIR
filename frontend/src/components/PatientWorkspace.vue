<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { backendJson, backendFetch, fhirJson } from '@/api/http'
import ResourceSection from '@/components/ResourceSection.vue'
import RecordFormModal from '@/components/RecordFormModal.vue'
import { patientCreateFields, patientUpdateFields, resourceConfigs, type ResourceConfig } from '@/config/resources'

type SessionRole = 'practitioner' | 'related-person'

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
}>()

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
  workspaceMessage.value = 'Child record created successfully.'
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
  workspaceMessage.value = 'Child profile updated successfully.'
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

  workspaceMessage.value = `${config.label} created successfully.`
  await loadResource(config)
}

async function updateResource(config: ResourceConfig, id: string, payload: Record<string, any>) {
  if (!config.updatePath) return

  await backendFetchForRole(config.updatePath(id), {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(enrichPayload(config.key, payload, selectedPatientId.value)),
  })

  workspaceMessage.value = `${config.label} updated successfully.`
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
  <div class="workspace-shell">
    <div class="workspace-grid">
      <va-card class="surface-card sidebar-card">
        <div class="section-header card-header">
          <div>
            <div class="kicker">{{ role === 'practitioner' ? 'Practitioner portal' : 'Related person portal' }}</div>
            <h2 class="card-title">{{ displayName }}</h2>
          </div>
        </div>

        <p class="intro-text">
          <template v-if="role === 'practitioner'">
            Search by child name, open a profile, then create or update the DTO-backed resources already exposed by the backend.
          </template>
          <template v-else>
            Choose one of your linked children and review the same clinical details in a clean, family-friendly workspace.
          </template>
        </p>

        <div class="metric-grid">
          <div class="metric">
            <div class="metric-value">{{ patients.length }}</div>
            <div class="metric-label">Loaded children</div>
          </div>
          <div class="metric">
            <div class="metric-value">{{ counts.immunizations ?? 0 }}</div>
            <div class="metric-label">Immunizations</div>
          </div>
          <div class="metric">
            <div class="metric-value">{{ linkedChildrenCount }}</div>
            <div class="metric-label">Linked children</div>
          </div>
        </div>

        <div class="toolbar">
          <va-input
              v-if="role === 'practitioner'"
              v-model="patientQuery"
              class="patient-search"
              placeholder="Search child by name or SVNR"
              @keyup.enter="loadPatients"
          />
          <va-button @click="loadPatients">
            {{ role === 'practitioner' ? 'Search' : 'Reload' }}
          </va-button>
          <va-button
              v-if="role === 'practitioner'"
              preset="secondary"
              @click="createPatientOpen = true"
          >
            Add child
          </va-button>
        </div>

        <va-alert v-if="patientsError" color="danger" outline class="alert-box">{{ patientsError }}</va-alert>

        <div v-if="patientsLoading" class="empty-state">Loading patients…</div>
        <div v-else-if="patients.length === 0" class="empty-state">No child records found.</div>
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
              <va-badge :text="patient.gender || 'unknown'" color="primary" />
            </div>
            <div class="patient-meta">
              <span v-if="patient.birthDate">DOB: {{ formatDate(patient.birthDate) }}</span>
              <span v-if="patient.svnr"> · SVNR: {{ patient.svnr }}</span>
            </div>
          </button>
        </div>
      </va-card>

      <va-card class="surface-card workspace-card">
        <div v-if="patientDetailsLoading" class="empty-state">Loading child workspace…</div>

        <template v-else-if="selectedPatientId && patientDetails">
          <div class="workspace-header">
            <div>
              <div class="kicker">Selected child</div>
              <h1 class="workspace-title">{{ patientDetails.fullName || selectedPatientId }}</h1>
              <div class="workspace-meta">
                <span>FHIR Patient ID: <code class="inline-code">{{ patientDetails.id || selectedPatientId }}</code></span>
                <span v-if="patientDetails.svnr"> · SVNR: {{ patientDetails.svnr }}</span>
              </div>
            </div>
            <div class="toolbar">
              <va-button
                  v-if="role === 'practitioner'"
                  preset="secondary"
                  @click="editPatientOpen = true"
              >
                Edit child profile
              </va-button>
              <va-button preset="secondary" @click="loadSelectedPatient">Refresh all</va-button>
            </div>
          </div>

          <va-alert v-if="workspaceMessage" color="success" outline class="alert-box">
            {{ workspaceMessage }}
          </va-alert>
          <va-alert v-if="patientDetailsError" color="danger" outline class="alert-box">
            {{ patientDetailsError }}
          </va-alert>

          <div class="scroll-tabs">
            <va-button
                :preset="selectedSection === 'overview' ? 'primary' : 'secondary'"
                @click="selectedSection = 'overview'"
            >
              Overview
            </va-button>
            <va-button
                v-for="config in resourceConfigs"
                :key="config.key"
                :preset="selectedSection === config.key ? 'primary' : 'secondary'"
                @click="selectedSection = config.key"
            >
              {{ config.label }}
            </va-button>
          </div>

          <section v-if="selectedSection === 'overview'" class="section-stack">
            <div class="metric-grid overview-grid">
              <div class="metric">
                <div class="metric-value">{{ counts.relatedPersons ?? 0 }}</div>
                <div class="metric-label">Related persons</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ counts.recommendations ?? 0 }}</div>
                <div class="metric-label">Recommendations</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ counts.goals ?? 0 }}</div>
                <div class="metric-label">Goal resources</div>
              </div>
              <div class="metric">
                <div class="metric-value">{{ counts.appointments ?? 0 }}</div>
                <div class="metric-label">Appointments</div>
              </div>
            </div>

            <article class="record-card">
              <div class="kicker">Child profile</div>
              <h2 class="record-title">Selected child overview</h2>
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
            <strong>Could not load the selected child workspace.</strong>
            <div class="error-text">{{ patientDetailsError }}</div>
          </div>
        </div>

        <div v-else class="empty-state">Select a child from the left side to open the portal workspace.</div>
      </va-card>
    </div>
  </div>

  <RecordFormModal
      v-model="createPatientOpen"
      title="Create child"
      :fields="patientCreateFields"
      @save="createPatient"
  />

  <RecordFormModal
      v-if="patientDetails && role === 'practitioner'"
      v-model="editPatientOpen"
      title="Update child"
      :fields="patientUpdateFields"
      :initial-value="patientDetails"
      @save="updatePatient"
  />
</template>

<style scoped>
.workspace-shell {
  width: calc(100vw - 40px);
  max-width: none;
  margin-left: calc(50% - 50vw + 20px);
  margin-right: calc(50% - 50vw + 20px);
}

.workspace-grid {
  display: grid;
  grid-template-columns: 420px minmax(0, 1fr);
  gap: 32px;
  align-items: start;
}

.surface-card {
  border-radius: 30px;
  border: 1px solid rgba(11, 45, 114, 0.12);
  box-shadow: 0 18px 42px rgba(11, 45, 114, 0.08);
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98) 0%, rgba(250, 253, 255, 0.98) 100%);
}

.sidebar-card,
.workspace-card {
  padding: 30px;
}

.sidebar-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(246, 231, 188, 0.22) 100%);
}

.workspace-card {
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.99) 0%, rgba(10, 196, 224, 0.07) 100%);
  min-height: 640px;
}

.card-header,
.workspace-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 18px;
  margin-bottom: 16px;
}

.card-title,
.record-title {
  margin: 6px 0 0;
  color: #0b2d72;
  font-size: 1.9rem;
  line-height: 1.2;
}

.workspace-title {
  margin: 6px 0 10px;
  color: #0b2d72;
  font-size: 2.35rem;
  line-height: 1.12;
}

.kicker {
  color: #0992c2;
  font-weight: 800;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  font-size: 0.86rem;
}

.intro-text,
.workspace-meta,
.patient-id,
.patient-meta {
  color: #5d7291;
}

.intro-text {
  margin: 0 0 20px;
  font-size: 1.08rem;
  line-height: 1.72;
}

.metric-grid {
  display: grid;
  grid-template-columns: 1fr;
  gap: 16px;
  margin-bottom: 22px;
}

.overview-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.metric {
  background: rgba(246, 231, 188, 0.22);
  border: 1px solid rgba(11, 45, 114, 0.1);
  border-radius: 24px;
  padding: 20px;
}

.metric-value {
  color: #0b2d72;
  font-size: 2.1rem;
  font-weight: 800;
  line-height: 1;
  margin-bottom: 7px;
}

.metric-label {
  color: #5d7291;
  font-size: 1rem;
}

.toolbar {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  align-items: center;
}

.patient-search {
  flex: 1 1 260px;
  min-width: 260px;
}

.alert-box {
  margin-bottom: 16px;
  border-radius: 16px;
}

.patient-list {
  display: grid;
  gap: 16px;
}

.patient-tile {
  width: 100%;
  text-align: left;
  background: #ffffff;
  border: 1px solid rgba(9, 146, 194, 0.24);
  border-radius: 24px;
  padding: 20px 20px 18px;
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
  background: linear-gradient(180deg, rgba(9, 146, 194, 0.08) 0%, rgba(10, 196, 224, 0.08) 100%);
  box-shadow: 0 12px 28px rgba(9, 146, 194, 0.14);
}

.patient-tile-top {
  display: flex;
  justify-content: space-between;
  gap: 14px;
}

.patient-name {
  display: block;
  font-size: 1.24rem;
  line-height: 1.25;
  color: #0b2d72;
}

.patient-id {
  margin-top: 4px;
  font-size: 0.98rem;
}

.patient-meta {
  margin-top: 10px;
  font-size: 0.98rem;
}

.scroll-tabs {
  display: flex;
  flex-wrap: wrap;
  gap: 14px;
  margin: 20px 0 26px;
}

.section-stack {
  display: grid;
  gap: 22px;
}

.record-card {
  background: #ffffff;
  border: 1px solid rgba(11, 45, 114, 0.1);
  border-radius: 26px;
  padding: 24px;
  box-shadow: 0 12px 26px rgba(11, 45, 114, 0.05);
}

.record-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
}

.record-item {
  display: flex;
  flex-direction: column;
  gap: 6px;
  background: rgba(10, 196, 224, 0.06);
  border: 1px solid rgba(9, 146, 194, 0.12);
  border-radius: 18px;
  padding: 16px;
  min-height: 82px;
}

.record-item strong {
  color: #0b2d72;
  font-size: 0.95rem;
  text-transform: capitalize;
}

.record-item span {
  color: #123053;
  font-size: 1rem;
  line-height: 1.45;
  word-break: break-word;
}

.inline-code {
  background: rgba(246, 231, 188, 0.85);
  color: #0b2d72;
  border-radius: 10px;
  padding: 3px 8px;
  font-size: 0.95rem;
}

.empty-state {
  display: grid;
  place-items: center;
  min-height: 220px;
  border: 1px dashed rgba(11, 45, 114, 0.2);
  border-radius: 22px;
  color: #5d7291;
  font-size: 1rem;
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
  min-height: 48px;
  padding: 0 20px;
  border-radius: 15px;
  font-weight: 700;
  letter-spacing: 0.01em;
}

:deep(.va-button--primary) {
  background: #0b2d72 !important;
  border-color: #0b2d72 !important;
  color: #ffffff !important;
}

:deep(.va-button--primary:hover) {
  background: #123b92 !important;
  border-color: #123b92 !important;
}

:deep(.va-button--secondary) {
  background: rgba(9, 146, 194, 0.08) !important;
  border-color: rgba(9, 146, 194, 0.22) !important;
  color: #0b2d72 !important;
}

:deep(.va-button--secondary:hover) {
  background: rgba(10, 196, 224, 0.12) !important;
  border-color: rgba(9, 146, 194, 0.3) !important;
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
    grid-template-columns: 380px minmax(0, 1fr);
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
    font-size: 1.8rem;
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
    font-size: 1.5rem;
  }

  .patient-tile-top,
  .workspace-header {
    flex-direction: column;
    align-items: flex-start;
  }
}
</style>
