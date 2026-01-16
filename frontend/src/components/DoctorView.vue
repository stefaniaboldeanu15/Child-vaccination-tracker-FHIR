<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <Card class="bg-white shadow-lg">
      <CardHeader class="border-b bg-gradient-to-r from-purple-50 to-blue-50">
        <div class="flex items-start justify-between">
          <div>
            <CardTitle>
              <span v-if="doctorDisplayName">Doctor Portal — {{ doctorDisplayName }}</span>
              <span v-else>Doctor Portal</span>
            </CardTitle>
            <CardDescription class="mt-2 space-y-1">
              <div>FHIR Practitioner ID: {{ doctorId || 'Unknown' }}</div>
              <div>Identifier: {{ doctorIdentifierLabel || 'None' }}</div>
            </CardDescription>
            <p v-if="doctorLoading" class="mt-3 text-sm text-muted-foreground">Loading practitioner data…</p>
            <p v-else-if="doctorError" class="mt-3 text-sm text-red-600">{{ doctorError }}</p>
          </div>
        </div>
      </CardHeader>
    </Card>

    <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
      <Card>
        <CardContent class="pt-6">
          <div class="text-center">
            <div class="text-blue-600">{{ patients.length }}</div>
            <div class="text-gray-500">Patients Loaded</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="pt-6">
          <div class="text-center">
            <div class="text-green-600">{{ lastQueryLabel }}</div>
            <div class="text-gray-500">Search Mode</div>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardContent class="pt-6">
          <div class="text-center">
            <div class="text-purple-600">{{ patientsLoading ? 'Loading…' : 'Ready' }}</div>
            <div class="text-gray-500">Status</div>
          </div>
        </CardContent>
      </Card>
    </div>

    <Card>
      <CardHeader>
        <CardTitle>Patient Search</CardTitle>
        <CardDescription>Search by name, identifier (system|value), or FHIR id.</CardDescription>
      </CardHeader>
      <CardContent>
        <div class="flex gap-2">
          <div class="relative flex-1">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input v-model="searchTerm" placeholder="e.g., Doe | 12345 | 591772" class="pl-10" />
          </div>
          <Button class="gap-2" @click="runSearch" :disabled="!searchTerm.trim() || patientsLoading">
            Search
          </Button>

          <Button variant="outline" class="gap-2" @click="createOpen = true" :disabled="patientsLoading">
            New patient
          </Button>
        </div>
        <p v-if="patientsError" class="mt-3 text-sm text-red-600">{{ patientsError }}</p>
      </CardContent>
    </Card>

    <div class="space-y-3">
      <h3 class="text-gray-700">
        {{ patients.length }} Patient{{ patients.length !== 1 ? 's' : '' }} Found
      </h3>

      <Card v-for="p in patients" :key="p.id" class="hover:shadow-md transition-shadow">
        <CardContent class="pt-6">
          <div class="flex items-center justify-between gap-3">
            <div class="space-y-2 flex-1 min-w-0">
              <div class="flex items-center gap-3">
                <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white">
                  {{ initials(p.display) }}
                </div>
                <div class="min-w-0">
                  <div class="text-gray-900 truncate">{{ p.display }}</div>
                  <div class="text-gray-500">FHIR ID: {{ p.id }}</div>
                </div>
              </div>

              <div class="flex flex-wrap gap-2 ml-15">
                <Badge v-if="p.birthDate" variant="outline" class="bg-blue-50">DOB: {{ p.birthDate }}</Badge>
                <Badge v-if="p.identifier" variant="outline" class="bg-green-50">{{ p.identifier }}</Badge>
                <Badge v-if="p.country" variant="outline" class="bg-purple-50">{{ p.country }}</Badge>
              </div>
            </div>

            <div class="flex gap-2">
              <Button variant="outline" size="sm" class="gap-2" @click="handleViewDetails(p.id)">
                <FileText class="w-4 h-4" />
                View Records
              </Button>

              <Button size="sm" class="gap-2" @click="handleAddVaccination(p.id)">
                <Calendar class="w-4 h-4" />
                Add Vaccination
              </Button>

              <Button variant="outline" size="sm" class="gap-2" @click="handleDownloadReport(p.id)">
                <Download class="w-4 h-4" />
                PDF
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>

    <AddVaccinationDialog
      v-if="selectedPatientId"
      :patient-id="selectedPatientId"
      :doctor-id="doctorId"
      :open="isAddDialogOpen"
      @update:open="isAddDialogOpen = $event"
      @saved="handleSaved"
    />

    <PatientDetailsDialog
      v-if="selectedPatientId"
      :patient-id="selectedPatientId"
      :open="isDetailsDialogOpen"
      @update:open="isDetailsDialogOpen = $event"
    />

    <CreatePatientDialog
      :open="createOpen"
      @update:open="createOpen = $event"
      @created="handlePatientCreated"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Search, Calendar, FileText, Download } from 'lucide-vue-next'

import { backendFetch, downloadPdf } from '@/api/backend'

import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Input from '@/components/ui/Input.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'

import AddVaccinationDialog from '@/components/AddVaccinationDialog.vue'
import PatientDetailsDialog from '@/components/PatientDetailsDialog.vue'
import CreatePatientDialog from '@/components/CreatePatientDialog.vue'

type PatientCandidate = {
  id: string
  display: string
  birthDate?: string
  identifier?: string
  country?: string
}

const props = defineProps<{ doctorId: string }>()

// Practitioner
const doctorResource = ref<any | null>(null)
const doctorLoading = ref(false)
const doctorError = ref<string | null>(null)

function pickDisplayName(p: any | null): string {
  const n = p?.name?.[0]
  if (!n) return ''
  if (typeof n.text === 'string' && n.text.trim()) return n.text.trim()
  const given = Array.isArray(n.given) ? n.given.join(' ') : ''
  const family = typeof n.family === 'string' ? n.family : ''
  return `${given} ${family}`.trim()
}

function pickIdentifierLabel(p: any | null): string {
  const ids = Array.isArray(p?.identifier) ? p.identifier : []
  const first = ids.find((i: any) => typeof i?.value === 'string' && i.value.trim())
  if (first?.system && first?.value) return `${first.system}|${first.value}`
  if (first?.value) return String(first.value)
  return ''
}

async function loadPractitioner() {
  const id = (props.doctorId || '').trim()
  if (!id) return
  doctorError.value = null
  doctorLoading.value = true
  try {
    const res = await backendFetch(`/Practitioner/${encodeURIComponent(id)}`)
    doctorResource.value = await res.json()
  } catch (e) {
    doctorResource.value = null
    doctorError.value = String(e)
  } finally {
    doctorLoading.value = false
  }
}

watch(
  () => props.doctorId,
  () => {
    void loadPractitioner()
  },
  { immediate: true },
)

const doctorDisplayName = computed(() => pickDisplayName(doctorResource.value))
const doctorIdentifierLabel = computed(() => pickIdentifierLabel(doctorResource.value))

// Patient search
const searchTerm = ref('')
const patients = ref<PatientCandidate[]>([])
const patientsLoading = ref(false)
const patientsError = ref<string | null>(null)
const lastQueryLabel = ref('—')

function initials(display: string) {
  const parts = (display || '').trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

function formatHumanName(res: any): string {
  const n = Array.isArray(res?.name) ? res.name[0] : null
  if (!n) return `Patient/${res?.id || ''}`
  if (typeof n.text === 'string' && n.text.trim()) return n.text.trim()
  const given = Array.isArray(n.given) ? n.given.join(' ') : ''
  const family = typeof n.family === 'string' ? n.family : ''
  const s = `${given} ${family}`.trim()
  return s || `Patient/${res?.id || ''}`
}

function formatIdentifier(res: any): string {
  const ids = Array.isArray(res?.identifier) ? res.identifier : []
  for (const id of ids) {
    const value = typeof id?.value === 'string' ? id.value.trim() : ''
    if (!value) continue
    const system = typeof id?.system === 'string' ? id.system.trim() : ''
    return system ? `${system}|${value}` : value
  }
  return ''
}

function pickCountry(res: any): string {
  const addrs = Array.isArray(res?.address) ? res.address : []
  const c = addrs.find((a: any) => typeof a?.country === 'string' && a.country.trim())?.country
  return typeof c === 'string' ? c.trim() : ''
}

function parsePatientBundle(bundle: any): PatientCandidate[] {
  const entries = Array.isArray(bundle?.entry) ? bundle.entry : []
  const out: PatientCandidate[] = []
  for (const e of entries) {
    const r = e?.resource
    if (!r || r.resourceType !== 'Patient') continue
    const id = String(r.id || '').trim()
    if (!id) continue
    out.push({
      id,
      display: formatHumanName(r),
      birthDate: typeof r.birthDate === 'string' ? r.birthDate : undefined,
      identifier: formatIdentifier(r) || undefined,
      country: pickCountry(r) || undefined,
    })
  }
  // de-dupe by id
  const seen = new Set<string>()
  return out.filter((p) => (seen.has(p.id) ? false : (seen.add(p.id), true)))
}

async function runSearch() {
  const q = searchTerm.value.trim()
  if (!q) return

  patientsError.value = null
  patientsLoading.value = true
  patients.value = []

  try {
    // Always search through backend convenience endpoint (avoids SPA index.html responses
    // when a dev-server proxy is misconfigured).
    // Backend applies simple heuristics (token => identifier, single token => _id then identifier, else name).
    const res = await backendFetch(`/api/physician/patients/search?term=${encodeURIComponent(q)}`)
    const ct = res.headers.get('content-type') ?? ''
    if (!ct.includes('json')) {
      const text = await res.text().catch(() => '')
      throw new Error(`Unexpected response (not JSON). Check that the request reaches the backend. Received content-type: ${ct}${text ? `\n${text.slice(0, 200)}` : ''}`)
    }
    const bundle = await res.json()
    // Best-effort label for the UI (mirrors backend heuristics)
    lastQueryLabel.value = q.includes('|') ? 'identifier' : (!q.includes(' ') && q.length <= 64 ? '_id/identifier' : 'name')
    patients.value = parsePatientBundle(bundle)
  } catch (e) {
    patientsError.value = String(e)
  } finally {
    patientsLoading.value = false
  }
}

// dialogs
const selectedPatientId = ref<string | null>(null)
const isAddDialogOpen = ref(false)
const isDetailsDialogOpen = ref(false)

// create patient
const createOpen = ref(false)

function handlePatientCreated(patientId: string) {
  // Search for the newly created patient and open their record.
  searchTerm.value = patientId
  void runSearch()
  selectedPatientId.value = patientId
  isDetailsDialogOpen.value = true
}

function handleAddVaccination(patientId: string) {
  selectedPatientId.value = patientId
  isAddDialogOpen.value = true
}

function handleViewDetails(patientId: string) {
  selectedPatientId.value = patientId
  isDetailsDialogOpen.value = true
}



async function handleDownloadReport(patientId: string) {
  try {
    const path = `/api/physician/patients/${encodeURIComponent(patientId)}/report`
    const name = `report-${patientId}.pdf`
    await downloadPdf(path, name)
  } catch (e) {
    window.alert(String(e))
  }
}

function handleSaved() {
  // best-effort refresh details dialog if open
  // PatientDetailsDialog loads on open, so toggling closed/open is enough.
  if (isDetailsDialogOpen.value) {
    isDetailsDialogOpen.value = false
    requestAnimationFrame(() => {
      isDetailsDialogOpen.value = true
    })
  }
}
</script>
