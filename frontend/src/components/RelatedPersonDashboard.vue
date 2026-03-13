<template>
  <div class="space-y-6">
    <Card class="bg-white shadow-lg">
      <CardHeader class="border-b bg-gradient-to-r from-purple-50 to-blue-50">
        <div class="flex flex-col gap-4 lg:flex-row lg:items-start lg:justify-between">
          <div class="flex items-start gap-3 min-w-0">
            <div class="flex h-14 w-14 shrink-0 items-center justify-center rounded-full bg-gradient-to-br from-blue-500 to-purple-500 text-white text-lg">
              {{ initials(patientDisplayName) }}
            </div>
            <div class="min-w-0 space-y-1">
              <CardTitle class="truncate">{{ patientDisplayName || 'Linked child' }}</CardTitle>
              <CardDescription class="space-y-1">
                <div class="truncate">FHIR Patient ID: {{ patient?.id || patientId }}</div>
                <div v-if="patient?.identifier">Identifier: {{ patient.identifier }}</div>
              </CardDescription>
            </div>
          </div>

          <div class="flex flex-wrap gap-2 lg:justify-end">
            <Badge v-if="patient?.gender" variant="outline" class="bg-white capitalize">{{ patient.gender }}</Badge>
            <Badge v-if="patient?.birthDate" variant="outline" class="bg-white">
              DOB: {{ formatDate(patient.birthDate) }}
            </Badge>
            <Badge v-if="patientAgeLabel" variant="outline" class="bg-white">Age: {{ patientAgeLabel }}</Badge>
            <Button variant="outline" size="sm" class="gap-2" @click="loadAll" :disabled="loading">
              <RefreshCw class="h-4 w-4" :class="{ 'animate-spin': loading }" />
              Refresh
            </Button>
          </div>
        </div>
      </CardHeader>
      <CardContent class="pt-6 space-y-4">
        <div class="grid grid-cols-2 gap-4 md:grid-cols-4">
          <div class="rounded-lg border border-green-200 bg-green-50 p-4">
            <div class="text-green-700">{{ completedImmunizations.length }}</div>
            <div class="text-gray-600">Completed Vaccinations</div>
          </div>
          <div class="rounded-lg border border-blue-200 bg-blue-50 p-4">
            <div class="text-blue-700">{{ upcomingAppointments.length }}</div>
            <div class="text-gray-600">Upcoming Appointments</div>
          </div>
          <div class="rounded-lg border border-purple-200 bg-purple-50 p-4">
            <div class="text-purple-700">{{ recommendations.length }}</div>
            <div class="text-gray-600">Recommendations</div>
          </div>
          <div class="rounded-lg border border-amber-200 bg-amber-50 p-4">
            <div class="text-amber-700">{{ allergies.length }}</div>
            <div class="text-gray-600">Allergies</div>
          </div>
        </div>

        <p v-if="loading" class="text-sm text-gray-500">Loading linked patient record…</p>
        <p v-else-if="error" class="whitespace-pre-wrap text-sm text-red-600">{{ error }}</p>
      </CardContent>
    </Card>

    <Tabs v-model="activeTab" defaultValue="overview" class="w-full">
      <TabsList class="grid h-auto w-full grid-cols-2 gap-1 md:grid-cols-4 lg:grid-cols-6">
        <TabsTrigger value="overview">Overview</TabsTrigger>
        <TabsTrigger value="immunizations">Immunizations</TabsTrigger>
        <TabsTrigger value="appointments">Appointments</TabsTrigger>
        <TabsTrigger value="recommendations">Recommendations</TabsTrigger>
        <TabsTrigger value="allergies">Allergies</TabsTrigger>
        <TabsTrigger value="encounters">Encounters</TabsTrigger>
      </TabsList>

      <TabsContent value="overview" class="mt-6 space-y-4">
        <Card>
          <CardHeader class="border-b">
            <CardTitle>Child summary</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div class="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-4">
              <div class="rounded-lg border p-4 space-y-1">
                <div class="text-sm text-gray-500">Name</div>
                <div class="text-gray-900">{{ patientDisplayName || '—' }}</div>
              </div>
              <div class="rounded-lg border p-4 space-y-1">
                <div class="text-sm text-gray-500">Birth date</div>
                <div class="text-gray-900">{{ patient?.birthDate ? formatDate(patient.birthDate) : '—' }}</div>
              </div>
              <div class="rounded-lg border p-4 space-y-1">
                <div class="text-sm text-gray-500">Gender</div>
                <div class="text-gray-900 capitalize">{{ patient?.gender || '—' }}</div>
              </div>
              <div class="rounded-lg border p-4 space-y-1">
                <div class="text-sm text-gray-500">Identifier</div>
                <div class="break-all text-gray-900">{{ patient?.identifier || '—' }}</div>
              </div>
            </div>
          </CardContent>
        </Card>

        <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
          <Card>
            <CardHeader class="border-b">
              <CardTitle>Upcoming appointments</CardTitle>
            </CardHeader>
            <CardContent class="pt-6">
              <div v-if="upcomingAppointments.length === 0" class="text-gray-500">No upcoming appointments scheduled.</div>
              <div v-else class="space-y-3">
                <div
                  v-for="appt in upcomingAppointments.slice(0, 3)"
                  :key="appt.id"
                  class="rounded-lg border p-4"
                >
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <div class="text-gray-900">{{ appt.reason || 'Appointment' }}</div>
                      <div class="text-sm text-gray-600">{{ formatDateTime(appt.start) }}</div>
                    </div>
                    <Badge variant="outline" :class="statusBadgeClass(appt.status)">{{ appt.status || 'unknown' }}</Badge>
                  </div>
                  <div class="mt-2 text-sm text-gray-600">
                    <span v-if="appt.location">Location: {{ appt.location }}</span>
                    <span v-if="appt.location && appt.practitionerName"> · </span>
                    <span v-if="appt.practitionerName">Practitioner: {{ appt.practitionerName }}</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader class="border-b">
              <CardTitle>Related contacts</CardTitle>
            </CardHeader>
            <CardContent class="pt-6">
              <div v-if="relatedPersons.length === 0" class="text-gray-500">No related person records found.</div>
              <div v-else class="space-y-3">
                <div
                  v-for="person in relatedPersons"
                  :key="person.id"
                  class="rounded-lg border p-4"
                >
                  <div class="flex flex-wrap items-center gap-2">
                    <div class="text-gray-900">{{ person.name || person.identifier || person.id }}</div>
                    <Badge v-if="person.relationship" variant="outline" class="bg-purple-50">{{ person.relationship }}</Badge>
                  </div>
                  <div class="mt-2 text-sm text-gray-600 flex flex-wrap gap-x-3 gap-y-1">
                    <span v-if="person.phone">Phone: {{ person.phone }}</span>
                    <span v-if="person.email">Email: {{ person.email }}</span>
                    <span v-if="person.identifier">ID: {{ person.identifier }}</span>
                  </div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>

        <div class="grid grid-cols-1 gap-4 lg:grid-cols-2">
          <Card>
            <CardHeader class="border-b">
              <CardTitle>Recent immunizations</CardTitle>
            </CardHeader>
            <CardContent class="pt-6">
              <div v-if="sortedImmunizations.length === 0" class="text-gray-500">No immunizations found.</div>
              <div v-else class="space-y-3">
                <div
                  v-for="imm in sortedImmunizations.slice(0, 4)"
                  :key="imm.id"
                  class="rounded-lg border p-4"
                >
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <div class="text-gray-900">{{ imm.vaccineDisplay || imm.vaccineCode || 'Immunization' }}</div>
                      <div class="text-sm text-gray-600">{{ imm.occurrenceDateTime ? formatDateTime(imm.occurrenceDateTime) : 'No date recorded' }}</div>
                    </div>
                    <Badge variant="outline" :class="statusBadgeClass(immunizationComputedStatus(imm))">
                      {{ immunizationComputedStatus(imm) }}
                    </Badge>
                  </div>
                  <div v-if="imm.practitionerName" class="mt-2 text-sm text-gray-600">Practitioner: {{ imm.practitionerName }}</div>
                </div>
              </div>
            </CardContent>
          </Card>

          <Card>
            <CardHeader class="border-b">
              <CardTitle>Upcoming recommendations</CardTitle>
            </CardHeader>
            <CardContent class="pt-6">
              <div v-if="sortedRecommendations.length === 0" class="text-gray-500">No recommendations available.</div>
              <div v-else class="space-y-3">
                <div
                  v-for="rec in sortedRecommendations.slice(0, 4)"
                  :key="rec.id"
                  class="rounded-lg border p-4"
                >
                  <div class="flex items-center justify-between gap-3">
                    <div>
                      <div class="text-gray-900">{{ rec.vaccineDisplay || rec.vaccineCode || 'Recommendation' }}</div>
                      <div class="text-sm text-gray-600">
                        Due: {{ rec.dueDate ? formatDate(rec.dueDate) : '—' }}
                        <span v-if="typeof rec.doseNumber === 'number'"> · Dose {{ rec.doseNumber }}</span>
                      </div>
                    </div>
                    <Badge variant="outline" :class="statusBadgeClass(rec.status)">{{ rec.status || 'suggested' }}</Badge>
                  </div>
                  <div v-if="rec.series" class="mt-2 text-sm text-gray-600">{{ rec.series }}</div>
                </div>
              </div>
            </CardContent>
          </Card>
        </div>
      </TabsContent>

      <TabsContent value="immunizations" class="mt-6 space-y-4">
        <Card>
          <CardHeader class="border-b">
            <CardTitle>Immunization history</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div class="mb-4 flex flex-wrap gap-2">
              <Badge variant="outline" class="bg-green-50">Completed: {{ completedImmunizations.length }}</Badge>
              <Badge variant="outline" class="bg-blue-50">Upcoming: {{ plannedImmunizations.length }}</Badge>
              <Badge variant="outline" class="bg-purple-50">Total: {{ immunizations.length }}</Badge>
            </div>

            <div v-if="sortedImmunizations.length === 0" class="text-gray-500">No immunizations found.</div>
            <div v-else class="space-y-3">
              <Card v-for="imm in sortedImmunizations" :key="imm.id">
                <CardContent class="pt-6">
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <div class="text-gray-900">{{ imm.vaccineDisplay || imm.vaccineCode || 'Unknown vaccine' }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(immunizationComputedStatus(imm))">
                          {{ immunizationComputedStatus(imm) }}
                        </Badge>
                      </div>
                      <div class="text-sm text-gray-600">
                        {{ imm.occurrenceDateTime ? formatDateTime(imm.occurrenceDateTime) : 'No date recorded' }}
                      </div>
                      <div class="text-sm text-gray-600" v-if="imm.practitionerName">Practitioner: {{ imm.practitionerName }}</div>
                    </div>
                    <div class="flex flex-wrap gap-2">
                      <Badge v-if="imm.vaccineCode" variant="outline" class="bg-white">Code: {{ imm.vaccineCode }}</Badge>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="appointments" class="mt-6 space-y-4">
        <Card>
          <CardHeader class="border-b">
            <CardTitle>Appointments</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div v-if="sortedAppointments.length === 0" class="text-gray-500">No appointments found.</div>
            <div v-else class="space-y-3">
              <Card v-for="appt in sortedAppointments" :key="appt.id">
                <CardContent class="pt-6">
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <div class="text-gray-900">{{ appt.reason || 'Appointment' }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(appt.status)">{{ appt.status || 'unknown' }}</Badge>
                      </div>
                      <div class="text-sm text-gray-600">
                        {{ formatDateTime(appt.start) }}
                        <span v-if="appt.end"> → {{ formatDateTime(appt.end) }}</span>
                      </div>
                      <div class="text-sm text-gray-600">
                        <span v-if="appt.location">Location: {{ appt.location }}</span>
                        <span v-if="appt.location && appt.practitionerName"> · </span>
                        <span v-if="appt.practitionerName">Practitioner: {{ appt.practitionerName }}</span>
                      </div>
                    </div>
                    <div class="text-xs text-gray-500 break-all">ID: {{ appt.id }}</div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="recommendations" class="mt-6 space-y-4">
        <Card>
          <CardHeader class="border-b">
            <CardTitle>Recommendations</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div v-if="sortedRecommendations.length === 0" class="text-gray-500">No recommendations found.</div>
            <div v-else class="space-y-3">
              <Card v-for="rec in sortedRecommendations" :key="rec.id">
                <CardContent class="pt-6">
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <div class="text-gray-900">{{ rec.vaccineDisplay || rec.vaccineCode || 'Recommendation' }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(rec.status)">{{ rec.status || 'suggested' }}</Badge>
                      </div>
                      <div class="text-sm text-gray-600">
                        Due: {{ rec.dueDate ? formatDate(rec.dueDate) : '—' }}
                        <span v-if="typeof rec.doseNumber === 'number'"> · Dose {{ rec.doseNumber }}</span>
                        <span v-if="rec.series"> · {{ rec.series }}</span>
                      </div>
                    </div>
                    <div class="text-xs text-gray-500 break-all">ID: {{ rec.id }}</div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="allergies" class="mt-6 space-y-4">
        <Card>
          <CardHeader class="border-b">
            <CardTitle>Allergies and intolerances</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div v-if="allergies.length === 0" class="text-gray-500">No allergies recorded.</div>
            <div v-else class="space-y-3">
              <Card v-for="alg in allergies" :key="alg.id">
                <CardContent class="pt-6">
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <div class="text-gray-900">{{ alg.display || alg.code || 'Allergy / Intolerance' }}</div>
                        <Badge v-if="alg.clinicalStatus" variant="outline" :class="statusBadgeClass(alg.clinicalStatus)">
                          {{ alg.clinicalStatus }}
                        </Badge>
                        <Badge v-if="alg.verificationStatus" variant="outline" class="bg-white">
                          {{ alg.verificationStatus }}
                        </Badge>
                      </div>
                      <div class="text-sm text-gray-600 flex flex-wrap gap-x-3 gap-y-1">
                        <span v-if="alg.criticality">Criticality: {{ alg.criticality }}</span>
                        <span v-if="alg.severity">Severity: {{ alg.severity }}</span>
                        <span v-if="alg.reaction">Reaction: {{ alg.reaction }}</span>
                      </div>
                      <div v-if="alg.onsetDate" class="text-sm text-gray-600">Onset: {{ formatDate(alg.onsetDate) }}</div>
                    </div>
                    <div class="text-xs text-gray-500 break-all">ID: {{ alg.id }}</div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>
      </TabsContent>

      <TabsContent value="encounters" class="mt-6 space-y-4">
        <Card>
          <CardHeader class="border-b">
            <CardTitle>Encounters</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div v-if="sortedEncounters.length === 0" class="text-gray-500">No encounters found.</div>
            <div v-else class="space-y-3">
              <Card v-for="enc in sortedEncounters" :key="enc.id">
                <CardContent class="pt-6">
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <div class="text-gray-900">Encounter {{ enc.id }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(enc.status)">{{ enc.status || 'unknown' }}</Badge>
                      </div>
                      <div class="text-sm text-gray-600">
                        {{ formatDateTime(enc.startDateTime) }}
                        <span v-if="enc.endDateTime"> → {{ formatDateTime(enc.endDateTime) }}</span>
                      </div>
                      <div class="text-sm text-gray-600">
                        <span v-if="enc.locationName">Location: {{ enc.locationName }}</span>
                        <span v-if="enc.locationName && enc.organizationName"> · </span>
                        <span v-if="enc.organizationName">Organization: {{ enc.organizationName }}</span>
                      </div>
                    </div>
                    <div class="flex flex-wrap gap-2">
                      <Badge v-if="enc.classDisplay" variant="outline" class="bg-white">{{ enc.classDisplay }}</Badge>
                    </div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader class="border-b">
            <CardTitle>Adverse events</CardTitle>
          </CardHeader>
          <CardContent class="pt-6">
            <div v-if="sortedAdverseEvents.length === 0" class="text-gray-500">No adverse events recorded.</div>
            <div v-else class="space-y-3">
              <Card v-for="evt in sortedAdverseEvents" :key="evt.id">
                <CardContent class="pt-6">
                  <div class="flex flex-col gap-3 md:flex-row md:items-start md:justify-between">
                    <div class="space-y-1">
                      <div class="flex flex-wrap items-center gap-2">
                        <div class="text-gray-900">{{ evt.display || evt.code || 'Adverse event' }}</div>
                        <Badge v-if="evt.outcome" variant="outline" :class="statusBadgeClass(evt.outcome)">{{ evt.outcome }}</Badge>
                      </div>
                      <div class="text-sm text-gray-600">
                        <span v-if="evt.date">{{ formatDateTime(evt.date) }}</span>
                        <span v-if="evt.category"> · Category: {{ evt.category }}</span>
                      </div>
                      <div v-if="evt.description" class="text-sm text-gray-700">{{ evt.description }}</div>
                      <div v-if="evt.suspectImmunizationIds.length" class="text-xs text-gray-600 break-all">
                        Suspect immunizations: {{ evt.suspectImmunizationIds.join(', ') }}
                      </div>
                    </div>
                    <div class="text-xs text-gray-500 break-all">ID: {{ evt.id }}</div>
                  </div>
                </CardContent>
              </Card>
            </div>
          </CardContent>
        </Card>
      </TabsContent>
    </Tabs>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RefreshCw } from 'lucide-vue-next'

import { fhirFetch } from '@/api/backend'

import Badge from '@/components/ui/Badge.vue'
import Button from '@/components/ui/Button.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsContent from '@/components/ui/TabsContent.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'

type PatientSummary = {
  id: string
  identifier?: string
  firstName?: string
  lastName?: string
  birthDate?: string
  gender?: string
}

type RelatedPersonSummary = {
  id: string
  identifier?: string
  name?: string
  relationship?: string
  phone?: string
  email?: string
}

type ImmunizationSummary = {
  id: string
  vaccineCode?: string
  vaccineDisplay?: string
  occurrenceDateTime?: string
  status?: string
  practitionerRef?: string
  practitionerName?: string
}

type RecommendationSummary = {
  id: string
  vaccineCode?: string
  vaccineDisplay?: string
  dueDate?: string
  status?: string
  series?: string
  doseNumber?: number
}

type AppointmentSummary = {
  id: string
  status?: string
  start?: string
  end?: string
  location?: string
  reason?: string
  practitionerRef?: string
  practitionerName?: string
}

type AllergySummary = {
  id: string
  clinicalStatus?: string
  verificationStatus?: string
  code?: string
  display?: string
  criticality?: string
  severity?: string
  reaction?: string
  onsetDate?: string
}

type EncounterSummary = {
  id: string
  status?: string
  classDisplay?: string
  startDateTime?: string
  endDateTime?: string
  locationRef?: string
  locationName?: string
  organizationRef?: string
  organizationName?: string
}

type AdverseEventSummary = {
  id: string
  category?: string
  outcome?: string
  date?: string
  code?: string
  display?: string
  description?: string
  suspectImmunizationIds: string[]
}

const props = defineProps<{ patientId: string }>()

const activeTab = ref('overview')
const loading = ref(false)
const error = ref<string | null>(null)

const patient = ref<PatientSummary | null>(null)
const relatedPersons = ref<RelatedPersonSummary[]>([])
const immunizations = ref<ImmunizationSummary[]>([])
const recommendations = ref<RecommendationSummary[]>([])
const appointments = ref<AppointmentSummary[]>([])
const allergies = ref<AllergySummary[]>([])
const encounters = ref<EncounterSummary[]>([])
const adverseEvents = ref<AdverseEventSummary[]>([])

const referenceLabelCache = new Map<string, string>()

const patientDisplayName = computed(() => {
  const p = patient.value
  if (!p) return ''
  const display = [p.firstName, p.lastName].filter(Boolean).join(' ').trim()
  return display || `Patient/${p.id}`
})

const patientAgeLabel = computed(() => ageLabelFromDate(patient.value?.birthDate))

const sortedImmunizations = computed(() =>
  [...immunizations.value].sort((a, b) => compareDateDesc(a.occurrenceDateTime, b.occurrenceDateTime)),
)

const completedImmunizations = computed(() =>
  sortedImmunizations.value.filter((item) => immunizationComputedStatus(item) === 'completed'),
)

const plannedImmunizations = computed(() =>
  sortedImmunizations.value.filter((item) => immunizationComputedStatus(item) !== 'completed'),
)

const sortedRecommendations = computed(() =>
  [...recommendations.value].sort((a, b) => compareDateAsc(a.dueDate, b.dueDate)),
)

const sortedAppointments = computed(() =>
  [...appointments.value].sort((a, b) => compareDateAsc(a.start, b.start)),
)

const upcomingAppointments = computed(() =>
  sortedAppointments.value.filter((item) => {
    const t = toTime(item.start)
    return Number.isFinite(t) && t >= Date.now()
  }),
)

const sortedEncounters = computed(() =>
  [...encounters.value].sort((a, b) => compareDateDesc(a.startDateTime, b.startDateTime)),
)

const sortedAdverseEvents = computed(() =>
  [...adverseEvents.value].sort((a, b) => compareDateDesc(a.date, b.date)),
)

function initials(display: string) {
  const parts = (display || '').trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return `${parts[0][0]}${parts[parts.length - 1][0]}`.toUpperCase()
}

function getTextCodeableConcept(value: any): string {
  if (!value) return ''
  if (typeof value.text === 'string' && value.text.trim()) return value.text.trim()
  const coding = Array.isArray(value.coding) ? value.coding[0] : null
  if (!coding) return ''
  return String(coding.display || coding.code || '').trim()
}

function getHumanName(value: any): string {
  if (!value) return ''
  const parts = [
    ...(Array.isArray(value.given) ? value.given : []),
    value.family,
  ].filter(Boolean)
  return parts.join(' ').trim()
}

function getReferenceId(reference?: string): string {
  if (!reference) return ''
  const normalized = reference.split('?')[0]
  const parts = normalized.split('/').filter(Boolean)
  return parts[parts.length - 1] || ''
}

function getReferenceKey(reference?: string): string {
  if (!reference) return ''
  const normalized = reference.replace(/^https?:\/\/[^/]+/i, '')
  return normalized.replace(/^\/+/, '')
}

function getIdentifier(resource: any, preferredSystem?: string): string {
  const identifiers = Array.isArray(resource?.identifier) ? resource.identifier : []
  if (preferredSystem) {
    const preferred = identifiers.find((item: any) => item?.system === preferredSystem && item?.value)
    if (preferred?.value) return String(preferred.value)
  }
  const first = identifiers.find((item: any) => item?.value)
  return first?.value ? String(first.value) : ''
}

function getTelecom(resource: any, system: string): string {
  const telecom = Array.isArray(resource?.telecom) ? resource.telecom : []
  const match = telecom.find((item: any) => item?.system === system && item?.value)
  return match?.value ? String(match.value) : ''
}

function getBundleResources<T = any>(payload: any): T[] {
  if (!payload || !Array.isArray(payload.entry)) return []
  return payload.entry.map((entry: any) => entry?.resource).filter(Boolean)
}

function parsePatient(resource: any): PatientSummary {
  return {
    id: String(resource?.id || props.patientId),
    identifier:
      getIdentifier(resource, 'https://elga.gv.at/svnr') ||
      getIdentifier(resource, 'http://example.org/patient-portal/username') ||
      getIdentifier(resource),
    firstName: Array.isArray(resource?.name?.[0]?.given) ? resource.name[0].given.join(' ').trim() : undefined,
    lastName: resource?.name?.[0]?.family ? String(resource.name[0].family) : undefined,
    birthDate: resource?.birthDate ? String(resource.birthDate) : undefined,
    gender: resource?.gender ? String(resource.gender) : undefined,
  }
}

function parseRelatedPerson(resource: any): RelatedPersonSummary {
  return {
    id: String(resource?.id || ''),
    identifier: getIdentifier(resource),
    name: getHumanName(resource?.name?.[0]),
    relationship: getTextCodeableConcept(Array.isArray(resource?.relationship) ? resource.relationship[0] : undefined),
    phone: getTelecom(resource, 'phone'),
    email: getTelecom(resource, 'email'),
  }
}

function parseImmunization(resource: any): ImmunizationSummary {
  const vaccineCode = Array.isArray(resource?.vaccineCode?.coding) ? resource.vaccineCode.coding[0] : null
  const performer = Array.isArray(resource?.performer) ? resource.performer[0] : null
  return {
    id: String(resource?.id || ''),
    vaccineCode: vaccineCode?.code ? String(vaccineCode.code) : undefined,
    vaccineDisplay: getTextCodeableConcept(resource?.vaccineCode),
    occurrenceDateTime: resource?.occurrenceDateTime ? String(resource.occurrenceDateTime) : undefined,
    status: resource?.status ? String(resource.status) : undefined,
    practitionerRef: performer?.actor?.reference ? String(performer.actor.reference) : undefined,
  }
}

function parseRecommendation(resource: any): RecommendationSummary[] {
  const recs = Array.isArray(resource?.recommendation) ? resource.recommendation : []
  return recs.map((item: any, index: number) => {
    const vaccine = Array.isArray(item?.vaccineCode) ? item.vaccineCode[0] : undefined
    const vaccineCoding = Array.isArray(vaccine?.coding) ? vaccine.coding[0] : null
    const dueCriterion = Array.isArray(item?.dateCriterion)
      ? item.dateCriterion.find((criterion: any) => getTextCodeableConcept(criterion?.code).toLowerCase().includes('due')) ?? item.dateCriterion[0]
      : null

    const doseNumberRaw = item?.doseNumber?.value ?? item?.doseNumber
    const parsedDoseNumber = Number.parseInt(String(doseNumberRaw), 10)

    return {
      id: `${String(resource?.id || 'recommendation')}-${index + 1}`,
      vaccineCode: vaccineCoding?.code ? String(vaccineCoding.code) : undefined,
      vaccineDisplay: getTextCodeableConcept(vaccine),
      dueDate: dueCriterion?.value ? String(dueCriterion.value) : undefined,
      status: getTextCodeableConcept(item?.forecastStatus) || undefined,
      series: item?.series ? String(item.series) : undefined,
      doseNumber: Number.isFinite(parsedDoseNumber) ? parsedDoseNumber : undefined,
    }
  })
}

function parseAppointment(resource: any): AppointmentSummary {
  const participants = Array.isArray(resource?.participant) ? resource.participant : []
  const practitionerParticipant = participants.find((item: any) => String(item?.actor?.reference || '').startsWith('Practitioner/'))
  const reasons = Array.isArray(resource?.reason) ? resource.reason : []
  const reason = reasons[0]?.concept ? getTextCodeableConcept(reasons[0].concept) : ''

  return {
    id: String(resource?.id || ''),
    status: resource?.status ? String(resource.status) : undefined,
    start: resource?.start ? String(resource.start) : undefined,
    end: resource?.end ? String(resource.end) : undefined,
    location: resource?.description ? String(resource.description) : undefined,
    reason: reason || (resource?.description ? String(resource.description) : undefined),
    practitionerRef: practitionerParticipant?.actor?.reference ? String(practitionerParticipant.actor.reference) : undefined,
  }
}

function parseAllergy(resource: any): AllergySummary {
  const reaction = Array.isArray(resource?.reaction) ? resource.reaction[0] : null
  return {
    id: String(resource?.id || ''),
    clinicalStatus: getTextCodeableConcept(resource?.clinicalStatus) || undefined,
    verificationStatus: getTextCodeableConcept(resource?.verificationStatus) || undefined,
    code: Array.isArray(resource?.code?.coding) ? resource.code.coding[0]?.code : undefined,
    display: getTextCodeableConcept(resource?.code),
    criticality: resource?.criticality ? String(resource.criticality) : undefined,
    severity: reaction?.severity ? String(reaction.severity) : undefined,
    reaction: reaction?.description ? String(reaction.description) : undefined,
    onsetDate: resource?.onsetDateTime ? String(resource.onsetDateTime) : resource?.onsetDate ? String(resource.onsetDate) : undefined,
  }
}

function parseEncounter(resource: any): EncounterSummary {
  const location = Array.isArray(resource?.location) ? resource.location[0] : null
  const actualPeriod = resource?.actualPeriod || resource?.period
  return {
    id: String(resource?.id || ''),
    status: resource?.status ? String(resource.status) : undefined,
    classDisplay: getTextCodeableConcept(resource?.class) || undefined,
    startDateTime: actualPeriod?.start ? String(actualPeriod.start) : undefined,
    endDateTime: actualPeriod?.end ? String(actualPeriod.end) : undefined,
    locationRef: location?.location?.reference ? String(location.location.reference) : undefined,
    organizationRef: resource?.serviceProvider?.reference ? String(resource.serviceProvider.reference) : undefined,
  }
}

function parseAdverseEvent(resource: any): AdverseEventSummary {
  const suspectEntity = Array.isArray(resource?.suspectEntity) ? resource.suspectEntity : []
  return {
    id: String(resource?.id || ''),
    category: getTextCodeableConcept(Array.isArray(resource?.category) ? resource.category[0] : undefined) || undefined,
    outcome: getTextCodeableConcept(Array.isArray(resource?.outcome) ? resource.outcome[0] : undefined) || undefined,
    date: resource?.recordedDate ? String(resource.recordedDate) : resource?.occurrenceDateTime ? String(resource.occurrenceDateTime) : undefined,
    code: Array.isArray(resource?.event?.coding) ? resource.event.coding[0]?.code : undefined,
    display: getTextCodeableConcept(resource?.event) || undefined,
    description: resource?.resultingEffect?.[0]?.text ? String(resource.resultingEffect[0].text) : undefined,
    suspectImmunizationIds: suspectEntity
      .map((item: any) => getReferenceId(item?.instance?.reference))
      .filter(Boolean),
  }
}

async function resolveReferenceLabel(reference?: string): Promise<string> {
  const key = getReferenceKey(reference)
  if (!key) return ''
  if (referenceLabelCache.has(key)) return referenceLabelCache.get(key) ?? ''

  try {
    const res = await fhirFetch(`/${key}`)
    const resource = await res.json()

    let label = ''
    switch (resource?.resourceType) {
      case 'Practitioner':
      case 'RelatedPerson':
      case 'Patient':
        label = getHumanName(resource?.name?.[0]) || getIdentifier(resource) || getReferenceId(reference)
        break
      case 'Organization':
      case 'Location':
        label = resource?.name ? String(resource.name) : getReferenceId(reference)
        break
      default:
        label = getReferenceId(reference)
        break
    }

    referenceLabelCache.set(key, label)
    return label
  } catch {
    return getReferenceId(reference) || ''
  }
}

async function enrichReferenceLabels() {
  await Promise.all([
    ...immunizations.value.map(async (item) => {
      if (!item.practitionerRef) return
      item.practitionerName = await resolveReferenceLabel(item.practitionerRef)
    }),
    ...appointments.value.map(async (item) => {
      if (!item.practitionerRef) return
      item.practitionerName = await resolveReferenceLabel(item.practitionerRef)
    }),
    ...encounters.value.map(async (item) => {
      if (item.locationRef) item.locationName = await resolveReferenceLabel(item.locationRef)
      if (item.organizationRef) item.organizationName = await resolveReferenceLabel(item.organizationRef)
    }),
  ])
}

function toTime(value?: string | null): number {
  if (!value) return Number.NaN
  const t = new Date(value).getTime()
  return Number.isFinite(t) ? t : Number.NaN
}

function compareDateAsc(a?: string | null, b?: string | null) {
  const ta = toTime(a)
  const tb = toTime(b)
  if (!Number.isFinite(ta) && !Number.isFinite(tb)) return 0
  if (!Number.isFinite(ta)) return 1
  if (!Number.isFinite(tb)) return -1
  return ta - tb
}

function compareDateDesc(a?: string | null, b?: string | null) {
  const ta = toTime(a)
  const tb = toTime(b)
  if (!Number.isFinite(ta) && !Number.isFinite(tb)) return 0
  if (!Number.isFinite(ta)) return 1
  if (!Number.isFinite(tb)) return -1
  return tb - ta
}

function formatDate(value?: string | null) {
  if (!value) return '—'
  const isoMatch = String(value).match(/^(\d{4})-(\d{2})-(\d{2})/)
  if (isoMatch) {
    const [, year, month, day] = isoMatch
    return `${day}.${month}.${year}`
  }
  const date = new Date(value)
  if (!Number.isFinite(date.getTime())) return String(value)
  const day = String(date.getDate()).padStart(2, '0')
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const year = date.getFullYear()
  return `${day}.${month}.${year}`
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'
  const date = new Date(value)
  if (!Number.isFinite(date.getTime())) return String(value)
  return date.toLocaleString('de-AT', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  })
}

function ageLabelFromDate(value?: string | null): string {
  if (!value) return ''
  const dob = new Date(value)
  if (!Number.isFinite(dob.getTime())) return ''
  const now = new Date()
  let years = now.getFullYear() - dob.getFullYear()
  const monthDelta = now.getMonth() - dob.getMonth()
  if (monthDelta < 0 || (monthDelta === 0 && now.getDate() < dob.getDate())) years -= 1
  return years >= 0 ? `${years}y` : ''
}

function immunizationComputedStatus(item: ImmunizationSummary): string {
  const raw = String(item.status || '').trim().toLowerCase()
  if (raw) {
    const t = toTime(item.occurrenceDateTime)
    if (Number.isFinite(t) && t > Date.now()) return 'planned'
    return raw
  }
  const t = toTime(item.occurrenceDateTime)
  if (!Number.isFinite(t)) return 'unknown'
  return t > Date.now() ? 'planned' : 'completed'
}

function statusBadgeClass(status?: string) {
  const s = String(status || '').toLowerCase()
  if (['completed', 'done', 'administered', 'resolved', 'fulfilled', 'booked', 'accepted', 'confirmed'].includes(s)) {
    return 'border-green-200 bg-green-50 text-green-700'
  }
  if (['planned', 'proposed', 'scheduled', 'pending', 'due'].includes(s)) {
    return 'border-blue-200 bg-blue-50 text-blue-700'
  }
  if (['cancelled', 'canceled', 'entered-in-error', 'error', 'failed', 'rejected', 'noshow'].includes(s)) {
    return 'border-red-200 bg-red-50 text-red-700'
  }
  if (['active', 'in-progress', 'open'].includes(s)) {
    return 'border-amber-200 bg-amber-50 text-amber-700'
  }
  return 'bg-white'
}

async function loadAll() {
  if (!props.patientId?.trim()) return

  loading.value = true
  error.value = null

  const patientKey = encodeURIComponent(props.patientId)
  const patientRef = `Patient/${props.patientId}`

  const [
    patientResult,
    relatedPersonsResult,
    immunizationsResult,
    recommendationsResult,
    appointmentsResult,
    allergiesResult,
    encountersResult,
    adverseEventsResult,
  ] = await Promise.allSettled([
    fhirFetch(`/Patient/${patientKey}`),
    fhirFetch(`/RelatedPerson?patient=${encodeURIComponent(patientRef)}`),
    fhirFetch(`/Immunization?patient=${encodeURIComponent(patientRef)}`),
    fhirFetch(`/ImmunizationRecommendation?patient=${encodeURIComponent(patientRef)}`),
    fhirFetch(`/Appointment?actor=${encodeURIComponent(patientRef)}`),
    fhirFetch(`/AllergyIntolerance?patient=${encodeURIComponent(patientRef)}`),
    fhirFetch(`/Encounter?subject=${encodeURIComponent(patientRef)}`),
    fhirFetch(`/AdverseEvent?subject=${encodeURIComponent(patientRef)}`),
  ])

  const errors: string[] = []

  async function parseJson<T>(result: PromiseSettledResult<Response>, fallback: T, label: string): Promise<T> {
    if (result.status === 'rejected') {
      errors.push(`${label}: ${String(result.reason)}`)
      return fallback
    }

    try {
      return ((await result.value.json()) as T) ?? fallback
    } catch {
      errors.push(`${label}: failed to parse response`)
      return fallback
    }
  }

  const patientResource = await parseJson<any>(patientResult, null, 'patient')
  const relatedPersonsBundle = await parseJson<any>(relatedPersonsResult, null, 'related persons')
  const immunizationsBundle = await parseJson<any>(immunizationsResult, null, 'immunizations')
  const recommendationsBundle = await parseJson<any>(recommendationsResult, null, 'recommendations')
  const appointmentsBundle = await parseJson<any>(appointmentsResult, null, 'appointments')
  const allergiesBundle = await parseJson<any>(allergiesResult, null, 'allergies')
  const encountersBundle = await parseJson<any>(encountersResult, null, 'encounters')
  const adverseEventsBundle = await parseJson<any>(adverseEventsResult, null, 'adverse events')

  patient.value = patientResource ? parsePatient(patientResource) : null
  relatedPersons.value = getBundleResources(relatedPersonsBundle).map(parseRelatedPerson)
  immunizations.value = getBundleResources(immunizationsBundle).map(parseImmunization)
  recommendations.value = getBundleResources(recommendationsBundle).flatMap(parseRecommendation)
  appointments.value = getBundleResources(appointmentsBundle).map(parseAppointment)
  allergies.value = getBundleResources(allergiesBundle).map(parseAllergy)
  encounters.value = getBundleResources(encountersBundle).map(parseEncounter)
  adverseEvents.value = getBundleResources(adverseEventsBundle).map(parseAdverseEvent)

  await enrichReferenceLabels()

  error.value = errors.length > 0 ? errors.join('\n') : null
  loading.value = false
}

watch(
  () => props.patientId,
  (id) => {
    if (!id) return
    void loadAll()
  },
  { immediate: true },
)
</script>
