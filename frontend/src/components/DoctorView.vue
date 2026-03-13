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
        <div v-if="successMessage" class="mt-3 rounded-md border border-green-200 bg-green-50 px-3 py-2 text-sm text-green-700">
          {{ successMessage }}
        </div>
      </CardContent>
    </Card>

    <div class="space-y-3">
      <h3 class="text-gray-700">
        {{ patients.length }} Patient{{ patients.length !== 1 ? 's' : '' }} Found
      </h3>

      <Card v-for="p in patients" :key="p.id" class="hover:shadow-md transition-shadow">
        <CardContent class="pt-6">
<div class="flex flex-col gap-4 xl:flex-row xl:items-start xl:justify-between">
  <div class="space-y-3 flex-1 min-w-0">
    <div class="flex items-start gap-3">
                <div class="w-12 h-12 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white">
                  {{ initials(p.display) }}
                </div>
                <div class="min-w-0">
                  <div class="text-gray-900 truncate">{{ p.display }}</div>
                  <div class="text-gray-500">FHIR ID: {{ p.id }}</div>
                </div>
              </div>

              <div class="flex flex-wrap gap-2 ml-15">
                 <Badge v-if="p.birthDate" variant="outline" class="bg-blue-50">
  DOB: {{ formatBirthDate(p.birthDate) }}
</Badge>
                <Badge v-if="p.identifier" variant="outline" class="bg-green-50">{{ p.identifier }}</Badge>
                <Badge v-if="typeof p.relatedPersonsCount === 'number'" variant="outline" class="bg-purple-50">
                  Related Persons: {{ p.relatedPersonsCount }}
                </Badge>
              </div>
            </div>

            <div class="flex flex-wrap gap-2 justify-end">
              <Button variant="outline" size="sm" class="gap-2" @click="handleEditPatient(p)">
                <Pencil class="w-4 h-4" />
                Edit
              </Button>

              <Button variant="outline" size="sm" class="gap-2" @click="handleManageRelatedPersons(p)">
                <Users class="w-4 h-4" />
                Related
              </Button>

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
      @saved="handleVaccinationSaved"
    />

    <PatientDetailsDialog
      v-if="selectedPatientId"
      :key="detailsReloadKey"
      :patient-id="selectedPatientId"
      :open="isDetailsDialogOpen"
      @update:open="isDetailsDialogOpen = $event"
    />

    <CreatePatientDialog
      :open="createOpen"
      @update:open="createOpen = $event"
      @created="handlePatientCreated"
    />

    <EditPatientDialog
      v-if="editingPatient"
      :open="editPatientOpen"
      :patient-id="editingPatient.id"
      :initial-first-name="editingPatient.firstName"
      :initial-last-name="editingPatient.lastName"
      :initial-birth-date="editingPatient.birthDate"
      :initial-gender="editingPatient.gender"
      @update:open="editPatientOpen = $event"
      @saved="handlePatientEdited"
    />

    <ManageRelatedPersonsDialog
      v-if="managingRelatedPatient"
      :open="manageRelatedOpen"
      :patient-id="managingRelatedPatient.id"
      :related-persons="managingRelatedPatient.relatedPersons ?? []"
      @update:open="manageRelatedOpen = $event"
      @changed="handleRelatedPersonsChanged"
    />
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Search, Calendar, FileText, Pencil, Users } from 'lucide-vue-next'

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
import EditPatientDialog from '@/components/EditPatientDialog.vue'
import ManageRelatedPersonsDialog from '@/components/ManageRelatedPersonsDialog.vue'

type PatientDetailsDTO = {
  patientId: string
  patientIdentifier?: string
  firstName?: string
  lastName?: string
  birthDate?: string | Date
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
  firstName?: string
  lastName?: string
  gender?: string
}

const props = defineProps<{ doctorId: string }>()

const { state } = useAuth()

const doctorDisplayName = computed(() => {
  const p = state.practitioner
  if (!p) return ''
  return [p.firstName, p.lastName].filter(Boolean).join(' ').trim()
})

const doctorIdentifierLabel = computed(() => state.practitioner?.practitionerIdentifier ?? '')

const searchTerm = ref('')
const patients = ref<PatientCandidate[]>([])
const patientsLoading = ref(false)
const patientsError = ref<string | null>(null)
const successMessage = ref('')
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

function formatBirthDate(value?: string | Date) {
  if (!value) return ''

  if (value instanceof Date && !Number.isNaN(value.getTime())) {
    const day = String(value.getDate()).padStart(2, '0')
    const month = String(value.getMonth() + 1).padStart(2, '0')
    const year = value.getFullYear()
    return `${day}.${month}.${year}`
  }

  const raw = String(value).trim()

  const isoMatch = raw.match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (isoMatch) {
    const [, year, month, day] = isoMatch
    return `${day}.${month}.${year}`
  }

  const javaDateMatch = raw.match(/^[A-Za-z]{3} ([A-Za-z]{3}) (\d{1,2}) .* (\d{4})$/)
  if (javaDateMatch) {
    const [, monthName, day, year] = javaDateMatch
    const months: Record<string, string> = {
      Jan: '01', Feb: '02', Mar: '03', Apr: '04',
      May: '05', Jun: '06', Jul: '07', Aug: '08',
      Sep: '09', Oct: '10', Nov: '11', Dec: '12',
    }
    return `${String(day).padStart(2, '0')}.${months[monthName]}.${year}`
  }

  const parsed = new Date(raw)
  if (!Number.isNaN(parsed.getTime())) {
    const day = String(parsed.getDate()).padStart(2, '0')
    const month = String(parsed.getMonth() + 1).padStart(2, '0')
    const year = parsed.getFullYear()
    return `${day}.${month}.${year}`
  }

  return raw
}

function rowToCandidate(row: PatientDashboardRowDTO): PatientCandidate {
  return {
    id: row.patient.patientId,
    display: displayNameFromPatient(row.patient),
    birthDate: row.patient.birthDate,
    identifier: row.patient.patientIdentifier,
    relatedPersonsCount: Array.isArray(row.relatedPersons) ? row.relatedPersons.length : 0,
    relatedPersons: row.relatedPersons ?? [],
    firstName: row.patient.firstName,
    lastName: row.patient.lastName,
    gender: row.patient.gender,
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

watch(
  () => props.doctorId,
  (id) => { if (!id) return; void loadPatients() },
  { immediate: true },
)

// Dialogs
const selectedPatientId = ref<string | null>(null)
const isAddDialogOpen = ref(false)
const isDetailsDialogOpen = ref(false)
const detailsReloadKey = ref(0)
const createOpen = ref(false)

// Edit patient
const editPatientOpen = ref(false)
const editingPatient = ref<PatientCandidate | null>(null)

// Manage related persons
const manageRelatedOpen = ref(false)
const managingRelatedPatientId = ref<string | null>(null)
const managingRelatedPatient = computed(() =>
  managingRelatedPatientId.value
    ? patients.value.find((p) => p.id === managingRelatedPatientId.value) ?? null
    : null
)

function handlePatientCreated(patientId: string) {
  successMessage.value = 'Patient created successfully.'
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

function handleEditPatient(p: PatientCandidate) {
  editingPatient.value = p
  editPatientOpen.value = true
}

function handlePatientEdited() {
  successMessage.value = 'Patient details updated.'
  void loadPatients()
}

function handleManageRelatedPersons(p: PatientCandidate) {
  managingRelatedPatientId.value = p.id
  manageRelatedOpen.value = true
}

function handleVaccinationSaved() {
  successMessage.value = 'Vaccination saved successfully.'
  if (isDetailsDialogOpen.value) {
    detailsReloadKey.value++
  }
}

function handleRelatedPersonsChanged() {
  successMessage.value = 'Related persons updated.'
  void loadPatients()
}
</script>
