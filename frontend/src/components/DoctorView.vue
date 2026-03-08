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
      </CardHeader>
      <CardContent>
        <div class="flex gap-2">
          <div class="relative flex-1">
            <Search class="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-gray-400" />
            <Input v-model="searchTerm" placeholder="Enter SVNR" class="pl-10" @keydown.enter="runSearch" />
          </div>

          <Button class="gap-2" @click="runSearch" :disabled="!searchTerm.trim() || patientsLoading">
            Search
          </Button>

          <Button variant="outline" class="gap-2" @click="loadPatients" :disabled="patientsLoading">
            Reload
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
                <Badge v-if="typeof p.relatedPersonsCount === 'number'" variant="outline" class="bg-purple-50">
                  Related Persons: {{ p.relatedPersonsCount }}
                </Badge>
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
            </div>
          </div>
        </CardContent>
      </Card>

      <Card v-if="!patientsLoading && !patientsError && patients.length === 0">
        <CardContent class="pt-6 text-gray-500">
          No patients found.
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
import { Search, Calendar, FileText } from 'lucide-vue-next'

import { backendFetch } from '@/api/backend'
import { useAuth } from '@/auth/auth'

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

type PatientDetailsDTO = {
  patientId: string
  patientIdentifier?: string
  firstName?: string
  lastName?: string
  birthDate?: string
  gender?: string
}

type RelatedPersonDTO = {
  relatedPersonId: string
  relatedPersonIdentifier?: string
  relationship?: string
  fullName?: string
  phone?: string
  email?: string
  address?: string
}

type PatientDashboardRowDTO = {
  patient: PatientDetailsDTO
  relatedPersons: RelatedPersonDTO[]
}

type PatientCandidate = {
  id: string
  display: string
  birthDate?: string
  identifier?: string
  relatedPersonsCount?: number
  relatedPersons?: RelatedPersonDTO[]
}

const props = defineProps<{ doctorId: string }>()

const { state } = useAuth()


const doctorLoading = ref(false)
const doctorError = ref<string | null>(null)

const doctorDisplayName = computed(() => {
  const p = state.practitioner
  if (!p) return ''
  return [p.firstName, p.lastName].filter(Boolean).join(' ').trim()
})

const doctorIdentifierLabel = computed(() => state.practitioner?.practitionerIdentifier ?? '')

// Patient search/dashboard list
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

function displayNameFromPatient(p: PatientDetailsDTO): string {
  return [p.firstName, p.lastName].filter(Boolean).join(' ').trim() || `Patient/${p.patientId}`
}

function rowToCandidate(row: PatientDashboardRowDTO): PatientCandidate {
  return {
    id: row.patient.patientId,
    display: displayNameFromPatient(row.patient),
    birthDate: row.patient.birthDate,
    identifier: row.patient.patientIdentifier,
    relatedPersonsCount: Array.isArray(row.relatedPersons) ? row.relatedPersons.length : 0,
    relatedPersons: row.relatedPersons ?? [],
  }
}

async function loadPatients() {
  patientsError.value = null
  patientsLoading.value = true
  try {
    const res = await backendFetch('/api/practitioner/dashboard/patients')
    const rows = (await res.json()) as PatientDashboardRowDTO[]
    patients.value = rows.map(rowToCandidate)
    lastQueryLabel.value = 'dashboard'
  } catch (e) {
    patientsError.value = String(e)
    patients.value = []
  } finally {
    patientsLoading.value = false
  }
}

async function runSearch() {
  const q = searchTerm.value.trim()
  if (!q) return

  patientsError.value = null
  patientsLoading.value = true
  patients.value = []

  try {
    const res = await backendFetch(`/api/practitioner/dashboard/search?svnr=${encodeURIComponent(q)}`)
    const rows = (await res.json()) as PatientDashboardRowDTO[]
    patients.value = rows.map(rowToCandidate)
    lastQueryLabel.value = 'svnr'
  } catch (e) {
    patientsError.value = String(e)
  } finally {
    patientsLoading.value = false
  }
}

// Initial load + reload if practitioner context changes
watch(
  () => props.doctorId,
  (id) => {
    if (!id) return
    void loadPatients()
  },
  { immediate: true },
)

// dialogs
const selectedPatientId = ref<string | null>(null)
const isAddDialogOpen = ref(false)
const isDetailsDialogOpen = ref(false)

// create patient
const createOpen = ref(false)

function handlePatientCreated(patientId: string) {
  void loadPatients()
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

function handleSaved() {
  // Refresh details dialog if open (it loads on open)
  if (isDetailsDialogOpen.value) {
    isDetailsDialogOpen.value = false
    requestAnimationFrame(() => {
      isDetailsDialogOpen.value = true
    })
  }
}
</script>
