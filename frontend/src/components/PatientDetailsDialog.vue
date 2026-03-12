<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent v-if="open" class="max-w-6xl max-h-[90vh] overflow-y-auto">
      <DialogHeader>
        <div class="flex items-start justify-between gap-3">
          <div>
            <DialogTitle>Patient Medical Record</DialogTitle>
            <DialogDescription>Practitioner view of backend-supported patient details and clinical records</DialogDescription>
          </div>

          <Button variant="outline" size="sm" class="gap-2" @click="loadAll" :disabled="loading || !patientId">
            <RefreshCw class="w-4 h-4" :class="{ 'animate-spin': loading }" />
            Refresh
          </Button>
        </div>
      </DialogHeader>

      <div class="space-y-6">
        <!-- Hero / patient summary -->
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 p-6 rounded-lg space-y-3">
          <div class="flex items-center justify-between gap-3">
            <div class="flex items-center gap-3 min-w-0">
              <div
                class="w-16 h-16 shrink-0 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white text-xl"
              >
                {{ initials(patientDisplayName) }}
              </div>
              <div class="min-w-0">
                <h3 class="text-gray-900 truncate">{{ patientDisplayName || 'Patient' }}</h3>
                <p class="text-gray-600 truncate">FHIR Patient ID: {{ patientId || 'Unknown' }}</p>
              </div>
            </div>

            <div class="flex items-center gap-2 flex-wrap justify-end">
              <Badge v-if="patient?.gender" variant="outline" class="bg-white capitalize">
                <User class="w-3 h-3 mr-1" />
                {{ patient.gender }}
              </Badge>
              <Badge v-if="patient?.patientIdentifier" variant="outline" class="bg-white">
                <Shield class="w-3 h-3 mr-1" />
                {{ patient.patientIdentifier }}
              </Badge>
            </div>
          </div>

          <Separator />

          <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-3 text-gray-700">
            <div class="flex items-center gap-2">
              <Calendar class="w-4 h-4 text-gray-500" />
              <span>
                DOB:
                <span v-if="patient?.birthDate">{{ formatDate(patient.birthDate) }}</span>
                <span v-else>Unknown</span>
                <span v-if="patientAgeLabel" class="text-gray-500">({{ patientAgeLabel }})</span>
              </span>
            </div>

            <div class="flex items-center gap-2">
              <User class="w-4 h-4 text-gray-500" />
              <span>Gender: {{ patient?.gender || 'Unknown' }}</span>
            </div>

            <div class="flex items-center gap-2">
              <FileText class="w-4 h-4 text-gray-500" />
              <span class="truncate">Identifier: {{ patient?.patientIdentifier || 'None' }}</span>
            </div>
          </div>

          <p v-if="loading" class="text-sm text-muted-foreground">Loading…</p>
          <p v-else-if="error" class="text-sm text-red-600 whitespace-pre-wrap">{{ error }}</p>
        </div>

        <!-- Stat cards -->
        <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div class="bg-green-50 p-4 rounded-lg border border-green-200">
            <div class="text-green-700">{{ completedImmunizations.length }}</div>
            <div class="text-gray-600">Completed Immunizations</div>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg border border-blue-200">
            <div class="text-blue-700">{{ upcomingImmunizations.length }}</div>
            <div class="text-gray-600">Upcoming / Planned</div>
          </div>
          <div class="bg-purple-50 p-4 rounded-lg border border-purple-200">
            <div class="text-purple-700">{{ encounters.length }}</div>
            <div class="text-gray-600">Encounters</div>
          </div>
          <div class="bg-amber-50 p-4 rounded-lg border border-amber-200">
            <div class="text-amber-700">{{ adverseEvents.length }}</div>
            <div class="text-gray-600">Adverse Events</div>
          </div>
        </div>

        <!-- Tabs -->
        <Tabs v-model="activeTab" defaultValue="overview" class="w-full">
          <TabsList class="grid w-full grid-cols-2 md:grid-cols-4 lg:grid-cols-7 h-auto gap-1">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="immunizations">Immunizations</TabsTrigger>
            <TabsTrigger value="encounters">Encounters</TabsTrigger>
            <TabsTrigger value="recommendations">Recommendations</TabsTrigger>
            <TabsTrigger value="appointments">Appointments</TabsTrigger>
            <TabsTrigger value="allergies">Allergies</TabsTrigger>
            <TabsTrigger value="adverse">Adverse Events</TabsTrigger>
          </TabsList>

          <!-- Overview -->
          <TabsContent value="overview" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b">
                <CardTitle>Patient Overview</CardTitle>
              </CardHeader>
              <CardContent class="pt-6">
                <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
                  <div class="rounded-lg border p-4 space-y-2">
                    <div class="text-sm text-gray-500">Name</div>
                    <div class="text-gray-900">{{ patientDisplayName || '—' }}</div>
                  </div>
                  <div class="rounded-lg border p-4 space-y-2">
                    <div class="text-sm text-gray-500">Patient Identifier</div>
                    <div class="text-gray-900 break-all">{{ patient?.patientIdentifier || '—' }}</div>
                  </div>
                  <div class="rounded-lg border p-4 space-y-2">
                    <div class="text-sm text-gray-500">Birth Date</div>
                    <div class="text-gray-900">{{ patient?.birthDate ? formatDate(patient.birthDate) : '—' }}</div>
                  </div>
                  <div class="rounded-lg border p-4 space-y-2">
                    <div class="text-sm text-gray-500">Gender</div>
                    <div class="text-gray-900 capitalize">{{ patient?.gender || '—' }}</div>
                  </div>
                </div>
              </CardContent>
            </Card>

            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b">
                <CardTitle>Encounter Blocks</CardTitle>
                <CardDescription>Overview-linked encounters with location/organization and nested immunization blocks</CardDescription>
              </CardHeader>
              <CardContent class="pt-6">
                <div v-if="overviewEncounterBlocks.length === 0" class="text-gray-500">No encounter blocks in overview.</div>

                <div v-else class="space-y-3">
                  <div
                    v-for="(block, blockIdx) in overviewEncounterBlocks"
                    :key="block.encounter?.encounterId || blockIdx"
                    class="rounded-lg border p-4"
                  >
                    <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-3">
                      <div class="space-y-2">
                        <div class="flex items-center gap-2 flex-wrap">
                          <div class="font-medium text-gray-900">
                            Encounter {{ block.encounter?.encounterId || '—' }}
                          </div>
                          <Badge variant="outline" :class="statusBadgeClass(block.encounter?.status)">
                            {{ block.encounter?.status || 'unknown' }}
                          </Badge>
                        </div>

                        <div class="text-sm text-gray-600 space-y-1">
                          <div>
                            {{ formatDateTime(block.encounter?.startDateTime) }}
                            <span v-if="block.encounter?.endDateTime">
                              → {{ formatDateTime(block.encounter?.endDateTime) }}
                            </span>
                          </div>
                          <div v-if="block.location?.name || block.organization?.name">
                            <span v-if="block.location?.name">Location: {{ block.location.name }}</span>
                            <span v-if="block.location?.name && block.organization?.name"> · </span>
                            <span v-if="block.organization?.name">Organization: {{ block.organization.name }}</span>
                          </div>
                        </div>
                      </div>

                      <div class="flex gap-2 flex-wrap">
                        <Badge variant="outline" class="bg-blue-50">
                          Immunizations: {{ block.immunizations?.length ?? 0 }}
                        </Badge>
                        <Badge variant="outline" class="bg-green-50">
                          Observations: {{ block.observations?.length ?? 0 }}
                        </Badge>
                      </div>
                    </div>

                    <div v-if="(block.immunizations?.length ?? 0) > 0" class="mt-3 space-y-2">
                      <div class="text-sm font-medium text-gray-700">Nested Immunizations</div>
                      <div class="space-y-2">
                        <div
                          v-for="(ib, ibIdx) in block.immunizations"
                          :key="ib.immunization?.immunizationId || ibIdx"
                          class="rounded-md border bg-gray-50 p-3"
                        >
                          <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-2">
                            <div>
                              <div class="text-gray-900">
                                {{ ib.immunization?.vaccineDisplay || ib.immunization?.vaccineCode || 'Immunization' }}
                              </div>
                              <div class="text-sm text-gray-600">
                                {{ ib.immunization?.occurrenceDateTime ? formatDateTime(ib.immunization.occurrenceDateTime) : 'No date' }}
                                <span v-if="ib.practitioner"> · by {{ practitionerName(ib.practitioner) }}</span>
                              </div>
                            </div>
                            <Badge variant="outline" :class="statusBadgeClass(ib.immunization?.status)">
                              {{ ib.immunization?.status || 'unknown' }}
                            </Badge>
                          </div>

                          <div v-if="(ib.observations?.length ?? 0) > 0" class="mt-2 text-xs text-gray-600">
                            {{ ib.observations!.length }} observation(s)
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <!-- Immunizations -->
          <TabsContent value="immunizations" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b">
                <CardTitle>Immunizations</CardTitle>
              </CardHeader>
              <CardContent class="pt-6">
                <div class="flex flex-wrap gap-2 mb-4">
                  <Badge variant="outline" class="bg-green-50">Completed: {{ completedImmunizations.length }}</Badge>
                  <Badge variant="outline" class="bg-blue-50">Upcoming/Planned: {{ upcomingImmunizations.length }}</Badge>
                  <Badge variant="outline" class="bg-purple-50">Total: {{ immunizations.length }}</Badge>
                </div>

                <div v-if="immunizations.length === 0" class="text-gray-500">No immunizations found.</div>

                <div v-else class="space-y-3">
                  <Card v-for="imm in sortedImmunizations" :key="imm.immunizationId">
                    <CardContent class="pt-6">
                      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                        <div class="space-y-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="text-gray-900">
                              {{ imm.vaccineDisplay || imm.vaccineCode || 'Unknown vaccine' }}
                            </div>
                            <Badge variant="outline" :class="statusBadgeClass(immunizationComputedStatus(imm))">
                              {{ immunizationComputedStatus(imm) }}
                            </Badge>
                          </div>
                          <div class="text-sm text-gray-600">
                            {{ imm.occurrenceDateTime ? formatDateTime(imm.occurrenceDateTime) : 'No date' }}
                          </div>
                          <div class="text-xs text-gray-500 break-all">
                            ID: {{ imm.immunizationId }}
                            <span v-if="imm.encounterId"> · Encounter: {{ imm.encounterId }}</span>
                            <span v-if="imm.organizationId"> · Org: {{ imm.organizationId }}</span>
                          </div>
                        </div>

                        <div class="flex gap-2 flex-wrap">
                          <Badge v-if="imm.vaccineCode" variant="outline" class="bg-white">
                            Code: {{ imm.vaccineCode }}
                          </Badge>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <!-- Encounters -->
          <TabsContent value="encounters" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b flex flex-row items-center justify-between">
                <CardTitle>Encounters</CardTitle>
                <Button size="sm" variant="outline" @click="createEncOpen = true">+ New Encounter</Button>
              </CardHeader>
              <CardContent class="pt-6">
                <div v-if="encounters.length === 0" class="text-gray-500">No encounters found.</div>

                <div v-else class="space-y-3">
                  <Card v-for="enc in sortedEncounters" :key="enc.encounterId">
                    <CardContent class="pt-6">
                      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                        <div class="space-y-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="text-gray-900">Encounter {{ enc.encounterId }}</div>
                            <Badge variant="outline" :class="statusBadgeClass(enc.status)">{{ enc.status || 'unknown' }}</Badge>
                          </div>
                          <div class="text-sm text-gray-600">
                            {{ formatDateTime(enc.startDateTime) }}
                            <span v-if="enc.endDateTime"> → {{ formatDateTime(enc.endDateTime) }}</span>
                          </div>
                          <div class="text-xs text-gray-500 break-all">Patient: {{ enc.patientId }}</div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <!-- Recommendations -->
          <TabsContent value="recommendations" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b flex flex-row items-center justify-between">
                <CardTitle>Recommendations</CardTitle>
                <Button size="sm" variant="outline" @click="createRecOpen = true">+ Add</Button>
              </CardHeader>
              <CardContent class="pt-6">
                <!-- Age-based recommendations -->
                <div v-if="ageBasedRecommendations.length > 0" class="mb-5">
                  <div class="text-sm text-gray-700 mb-2">Age-based suggestions</div>
                  <div class="space-y-2">
                    <div
                      v-for="rec in ageBasedRecommendations"
                      :key="rec.id"
                      class="flex items-center justify-between rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 gap-3"
                    >
                      <div class="space-y-0.5">
                        <div class="text-gray-900">{{ rec.vaccineDisplay || rec.vaccineCode || 'Recommendation' }}</div>
                        <div class="text-xs text-gray-600">
                          Due: {{ rec.dueDate ? formatDate(rec.dueDate) : '—' }}
                          <span v-if="typeof rec.doseNumber === 'number'"> · Dose {{ rec.doseNumber }}</span>
                        </div>
                      </div>
                      <Badge variant="outline" :class="statusBadgeClass(rec.status)">{{ rec.status || 'suggested' }}</Badge>
                    </div>
                  </div>
                  <Separator class="mt-4" />
                </div>

                <div v-if="recommendations.length === 0" class="text-gray-500">No saved recommendations.</div>

                <div v-else class="space-y-3">
                  <Card v-for="rec in sortedRecommendations" :key="rec.id">
                    <CardContent class="pt-6">
                      <div class="flex flex-col md:flex-row md:items-center md:justify-between gap-3">
                        <div class="space-y-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="text-gray-900">
                              {{ rec.vaccineDisplay || rec.vaccineCode || 'Recommendation' }}
                            </div>
                            <Badge variant="outline" :class="statusBadgeClass(rec.status)">{{ rec.status || 'unknown' }}</Badge>
                          </div>
                          <div class="text-sm text-gray-600">
                            Due: {{ rec.dueDate ? formatDate(rec.dueDate) : '—' }}
                            <span v-if="typeof rec.doseNumber === 'number'"> · Dose {{ rec.doseNumber }}</span>
                            <span v-if="rec.series"> · {{ rec.series }}</span>
                          </div>
                          <div class="text-xs text-gray-500 break-all">ID: {{ rec.id }}</div>
                        </div>
                        <Badge v-if="rec.vaccineCode" variant="outline" class="bg-white">
                          Code: {{ rec.vaccineCode }}
                        </Badge>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <!-- Appointments -->
          <TabsContent value="appointments" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b flex flex-row items-center justify-between">
                <CardTitle>Appointments</CardTitle>
                <Button size="sm" variant="outline" @click="createApptOpen = true">+ Schedule</Button>
              </CardHeader>
              <CardContent class="pt-6">
                <div v-if="appointments.length === 0" class="text-gray-500">No appointments found.</div>

                <div v-else class="space-y-3">
                  <Card v-for="appt in sortedAppointments" :key="appt.id">
                    <CardContent class="pt-6">
                      <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-3">
                        <div class="space-y-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="text-gray-900">{{ appt.reason || 'Appointment' }}</div>
                            <Badge variant="outline" :class="statusBadgeClass(appt.status)">
                              {{ appt.status || 'unknown' }}
                            </Badge>
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
                          <div class="text-xs text-gray-500 break-all">ID: {{ appt.id }}</div>
                        </div>

                        <!-- Status update buttons -->
                        <div class="flex flex-wrap gap-1 shrink-0">
                          <button
                            v-for="s in appointmentStatusOptions"
                            :key="s.value"
                            :disabled="appt.status === s.value || apptStatusUpdating === appt.id"
                            @click="updateAppointmentStatus(appt.id, s.value)"
                            class="rounded px-2 py-1 text-xs border transition-colors disabled:opacity-40"
                            :class="appt.status === s.value ? 'bg-gray-100 text-gray-500 cursor-default' : 'hover:bg-gray-50'"
                          >
                            {{ s.label }}
                          </button>
                        </div>
                      </div>
                      <p v-if="apptStatusError && apptStatusUpdating === appt.id" class="mt-2 text-xs text-red-600">{{ apptStatusError }}</p>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <!-- Allergies -->
          <TabsContent value="allergies" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b">
                <CardTitle>Allergies</CardTitle>
              </CardHeader>
              <CardContent class="pt-6">
                <div v-if="allergies.length === 0" class="text-gray-500">No allergies found.</div>

                <div v-else class="space-y-3">
                  <Card v-for="alg in allergies" :key="alg.allergyId">
                    <CardContent class="pt-6">
                      <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-3">
                        <div class="space-y-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="text-gray-900">{{ alg.display || alg.code || 'Allergy/Intolerance' }}</div>
                            <Badge v-if="alg.clinicalStatus" variant="outline" :class="statusBadgeClass(alg.clinicalStatus)">
                              {{ alg.clinicalStatus }}
                            </Badge>
                            <Badge v-if="alg.verificationStatus" variant="outline" class="bg-white">
                              {{ alg.verificationStatus }}
                            </Badge>
                          </div>

                          <div class="text-sm text-gray-600 flex flex-wrap gap-x-3 gap-y-1">
                            <span v-if="alg.severity">Severity: {{ alg.severity }}</span>
                            <span v-if="alg.criticality">Criticality: {{ alg.criticality }}</span>
                            <span v-if="alg.reaction">Reaction: {{ alg.reaction }}</span>
                          </div>

                          <div class="text-sm text-gray-600 flex flex-wrap gap-x-3 gap-y-1">
                            <span v-if="alg.onsetDate">Onset: {{ formatDate(alg.onsetDate) }}</span>
                            <span v-if="alg.recorder">Recorder: {{ alg.recorder }}</span>
                          </div>

                          <div class="text-xs text-gray-500 break-all">ID: {{ alg.allergyId }}</div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>

          <!-- Adverse events -->
          <TabsContent value="adverse" class="mt-6 space-y-4">
            <Card class="bg-white shadow-sm">
              <CardHeader class="border-b flex flex-row items-center justify-between">
                <CardTitle>Adverse Events</CardTitle>
                <Button size="sm" variant="outline" @click="createAdvOpen = true">+ Report</Button>
              </CardHeader>
              <CardContent class="pt-6">
                <div v-if="adverseEvents.length === 0" class="text-gray-500">No adverse events found.</div>

                <div v-else class="space-y-3">
                  <Card v-for="evt in sortedAdverseEvents" :key="evt.adverseEventId">
                    <CardContent class="pt-6">
                      <div class="flex flex-col md:flex-row md:items-start md:justify-between gap-3">
                        <div class="space-y-1">
                          <div class="flex items-center gap-2 flex-wrap">
                            <div class="text-gray-900">{{ evt.display || evt.code || 'Adverse Event' }}</div>
                            <Badge v-if="evt.severity" variant="outline" :class="statusBadgeClass(evt.severity)">
                              {{ evt.severity }}
                            </Badge>
                            <Badge v-if="evt.outcome" variant="outline" class="bg-white">
                              {{ evt.outcome }}
                            </Badge>
                          </div>

                          <div class="text-sm text-gray-600">
                            <span v-if="evt.date">{{ formatDateTime(evt.date) }}</span>
                            <span v-if="evt.category"> · Category: {{ evt.category }}</span>
                          </div>

                          <div v-if="evt.description" class="text-sm text-gray-700 whitespace-pre-wrap">
                            {{ evt.description }}
                          </div>

                          <div class="text-xs text-gray-500 break-all">
                            ID: {{ evt.adverseEventId }}
                            <span v-if="evt.immunizationId"> · Immunization: {{ evt.immunizationId }}</span>
                            <span v-if="evt.encounterId"> · Encounter: {{ evt.encounterId }}</span>
                          </div>

                          <div v-if="evt.suspectImmunizationIds?.length" class="text-xs text-gray-600 break-all">
                            Suspect immunizations: {{ evt.suspectImmunizationIds.join(', ') }}
                          </div>
                        </div>
                      </div>
                    </CardContent>
                  </Card>
                </div>
              </CardContent>
            </Card>
          </TabsContent>
        </Tabs>
      </div>
    </DialogContent>
  </Dialog>

  <!-- Sub-dialogs -->
  <CreateRecommendationDialog
    :patient-id="patientId"
    :open="createRecOpen"
    @update:open="createRecOpen = $event"
    @saved="loadAll"
  />
  <CreateAppointmentDialog
    :patient-id="patientId"
    :open="createApptOpen"
    @update:open="createApptOpen = $event"
    @saved="loadAll"
  />
  <CreateAdverseEventDialog
    :patient-id="patientId"
    :open="createAdvOpen"
    @update:open="createAdvOpen = $event"
    @saved="loadAll"
  />
  <CreateEncounterDialog
    :patient-id="patientId"
    :open="createEncOpen"
    @update:open="createEncOpen = $event"
    @saved="loadAll"
  />
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Calendar, FileText, RefreshCw, Shield, User } from 'lucide-vue-next'

import { backendFetch } from '@/api/backend'

import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'
import Badge from '@/components/ui/Badge.vue'
import Separator from '@/components/ui/Separator.vue'
import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Button from '@/components/ui/Button.vue'
import Tabs from '@/components/ui/Tabs.vue'
import TabsList from '@/components/ui/TabsList.vue'
import TabsTrigger from '@/components/ui/TabsTrigger.vue'
import TabsContent from '@/components/ui/TabsContent.vue'

import CreateRecommendationDialog from '@/components/CreateRecommendationDialog.vue'
import CreateAppointmentDialog from '@/components/CreateAppointmentDialog.vue'
import CreateAdverseEventDialog from '@/components/CreateAdverseEventDialog.vue'
import CreateEncounterDialog from '@/components/CreateEncounterDialog.vue'

type PatientDetailsDTO = {
  patientId: string
  patientIdentifier?: string
  firstName?: string
  lastName?: string
  birthDate?: string
  gender?: string
}

type PractitionerDTO = {
  practitionerId: string
  practitionerIdentifier?: string
  firstName?: string
  lastName?: string
}

type ImmunizationDTO = {
  immunizationId: string
  patientId?: string
  encounterId?: string
  organizationId?: string
  vaccineCode?: string
  vaccineDisplay?: string
  occurrenceDateTime?: string
  status?: string
}

type ObservationDTO = {
  observationId: string
  immunizationId?: string
  code?: string
  display?: string
  value?: string
  unit?: string
  effectiveDateTime?: string
}

type ImmunizationBlockDTO = {
  immunization?: ImmunizationDTO
  practitioner?: PractitionerDTO
  observations?: ObservationDTO[]
}

type EncounterDTO = {
  encounterId: string
  patientId?: string
  status?: string
  startDateTime?: string
  endDateTime?: string
}

type LocationDTO = {
  locationId: string
  organizationId?: string
  name?: string
  address?: string
  description?: string
}

type OrganizationDTO = {
  organizationId: string
  name?: string
}

type EncounterBlockDTO = {
  encounter?: EncounterDTO
  location?: LocationDTO
  organization?: OrganizationDTO
  immunizations?: ImmunizationBlockDTO[]
  observations?: ObservationDTO[]
}

type AllergyIntoleranceDTO = {
  allergyId: string
  patientId?: string
  clinicalStatus?: string
  verificationStatus?: string
  code?: string
  display?: string
  criticality?: string
  severity?: string
  reaction?: string
  onsetDate?: string
  recorder?: string
}

type PatientClinicalOverviewDTO = {
  patient?: PatientDetailsDTO
  encounters?: EncounterBlockDTO[]
  allergies?: AllergyIntoleranceDTO[]
}

type ImmunizationRecommendationDTO = {
  id: string
  patientId?: string
  vaccineCode?: string
  vaccineDisplay?: string
  dueDate?: string
  status?: string
  series?: string
  doseNumber?: number
}

type AppointmentDTO = {
  id: string
  patientId?: string
  patientName?: string
  practitionerId?: string
  practitionerName?: string
  start?: string
  end?: string
  status?: string
  location?: string
  reason?: string
}

type AdverseEventDTO = {
  adverseEventId: string
  patientId?: string
  encounterId?: string
  immunizationId?: string
  category?: string
  severity?: string
  outcome?: string
  date?: string
  code?: string
  display?: string
  description?: string
  suspectImmunizationIds?: string[]
}

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{ (e: 'update:open', v: boolean): void }>()

const activeTab = ref('overview')
const loading = ref(false)
const error = ref<string | null>(null)

const overview = ref<PatientClinicalOverviewDTO | null>(null)
const immunizations = ref<ImmunizationDTO[]>([])
const encounters = ref<EncounterDTO[]>([])
const recommendations = ref<ImmunizationRecommendationDTO[]>([])
const ageBasedRecommendations = ref<ImmunizationRecommendationDTO[]>([])
const appointments = ref<AppointmentDTO[]>([])
const allergies = ref<AllergyIntoleranceDTO[]>([])
const adverseEvents = ref<AdverseEventDTO[]>([])

// Sub-dialog open flags
const createRecOpen = ref(false)
const createApptOpen = ref(false)
const createAdvOpen = ref(false)
const createEncOpen = ref(false)

// Appointment status update
const apptStatusUpdating = ref<string | null>(null)
const apptStatusError = ref<string | null>(null)

const appointmentStatusOptions = [
  { value: 'booked', label: 'Booked' },
  { value: 'arrived', label: 'Arrived' },
  { value: 'fulfilled', label: 'Fulfilled' },
  { value: 'cancelled', label: 'Cancelled' },
  { value: 'noshow', label: 'No-show' },
]

async function updateAppointmentStatus(appointmentId: string, status: string) {
  apptStatusUpdating.value = appointmentId
  apptStatusError.value = null
  try {
    const res = await backendFetch(
      `/api/practitioner/appointments/${encodeURIComponent(appointmentId)}/status?status=${encodeURIComponent(status)}`,
      { method: 'PUT' },
    )
    const updated = (await res.json()) as AppointmentDTO
    const idx = appointments.value.findIndex((a) => a.id === appointmentId)
    if (idx !== -1) appointments.value[idx] = updated
  } catch (e: any) {
    apptStatusError.value = String(e?.message ?? e)
  } finally {
    apptStatusUpdating.value = null
  }
}

const patient = computed(() => overview.value?.patient ?? null)
const overviewEncounterBlocks = computed(() => overview.value?.encounters ?? [])

const patientDisplayName = computed(() => {
  const p = patient.value
  if (!p) return ''
  const name = [p.firstName, p.lastName].filter(Boolean).join(' ').trim()
  return name || `Patient/${p.patientId || props.patientId}`
})

const patientAgeLabel = computed(() => ageLabelFromDate(patient.value?.birthDate))

const sortedImmunizations = computed(() =>
  [...immunizations.value].sort((a, b) => compareDateDesc(a.occurrenceDateTime, b.occurrenceDateTime)),
)

const completedImmunizations = computed(() =>
  sortedImmunizations.value.filter((i) => immunizationComputedStatus(i) === 'completed'),
)

const upcomingImmunizations = computed(() =>
  sortedImmunizations.value.filter((i) => immunizationComputedStatus(i) !== 'completed'),
)

const sortedEncounters = computed(() =>
  [...encounters.value].sort((a, b) => compareDateDesc(a.startDateTime, b.startDateTime)),
)

const sortedRecommendations = computed(() =>
  [...recommendations.value].sort((a, b) => compareDateAsc(a.dueDate, b.dueDate)),
)

const sortedAppointments = computed(() =>
  [...appointments.value].sort((a, b) => compareDateAsc(a.start, b.start)),
)

const sortedAdverseEvents = computed(() =>
  [...adverseEvents.value].sort((a, b) => compareDateDesc(a.date, b.date)),
)

function initials(display: string) {
  const parts = (display || '').trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

function practitionerName(p?: PractitionerDTO) {
  if (!p) return ''
  const n = [p.firstName, p.lastName].filter(Boolean).join(' ').trim()
  return n || p.practitionerIdentifier || p.practitionerId || 'Practitioner'
}

function toTime(value?: string | null): number {
  if (!value) return Number.NaN
  const t = new Date(value).getTime()
  return Number.isFinite(t) ? t : Number.NaN
}

function compareDateDesc(a?: string | null, b?: string | null) {
  const ta = toTime(a); const tb = toTime(b)
  if (!Number.isFinite(ta) && !Number.isFinite(tb)) return 0
  if (!Number.isFinite(ta)) return 1
  if (!Number.isFinite(tb)) return -1
  return tb - ta
}

function compareDateAsc(a?: string | null, b?: string | null) {
  const ta = toTime(a); const tb = toTime(b)
  if (!Number.isFinite(ta) && !Number.isFinite(tb)) return 0
  if (!Number.isFinite(ta)) return 1
  if (!Number.isFinite(tb)) return -1
  return ta - tb
}

function formatDateTime(value?: string | null) {
  if (!value) return '—'
  const d = new Date(value)
  if (!Number.isFinite(d.getTime())) return String(value)
  return d.toLocaleString()
}

function formatDate(value?: string | Date) {
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

function ageLabelFromDate(value?: string | null): string {
  if (!value) return ''
  const dob = new Date(value)
  if (!Number.isFinite(dob.getTime())) return ''
  const now = new Date()
  let years = now.getFullYear() - dob.getFullYear()
  const m = now.getMonth() - dob.getMonth()
  if (m < 0 || (m === 0 && now.getDate() < dob.getDate())) years -= 1
  return years >= 0 ? `${years}y` : ''
}

function immunizationComputedStatus(i: ImmunizationDTO): string {
  const raw = String(i.status || '').trim().toLowerCase()
  if (raw) {
    const t = toTime(i.occurrenceDateTime)
    if (Number.isFinite(t) && t > Date.now()) return 'planned'
    return raw
  }
  const t = toTime(i.occurrenceDateTime)
  if (!Number.isFinite(t)) return 'unknown'
  return t > Date.now() ? 'planned' : 'completed'
}

function statusBadgeClass(status?: string) {
  const s = String(status || '').toLowerCase()
  if (['completed', 'done', 'administered', 'resolved', 'booked', 'fulfilled'].includes(s))
    return 'border-green-200 bg-green-50 text-green-700'
  if (['planned', 'proposed', 'scheduled', 'pending', 'arrived'].includes(s))
    return 'border-blue-200 bg-blue-50 text-blue-700'
  if (['cancelled', 'canceled', 'entered-in-error', 'error', 'failed', 'noshow'].includes(s))
    return 'border-red-200 bg-red-50 text-red-700'
  if (['in-progress', 'active', 'open', 'confirmed'].includes(s))
    return 'border-amber-200 bg-amber-50 text-amber-700'
  return 'bg-white'
}

async function loadAll() {
  if (!props.patientId?.trim()) return

  loading.value = true
  error.value = null
  const id = encodeURIComponent(props.patientId)

  const [
    overviewResult,
    immunizationsResult,
    encountersResult,
    recommendationsResult,
    ageRecsResult,
    appointmentsResult,
    allergiesResult,
    adverseResult,
  ] = await Promise.allSettled([
    backendFetch(`/api/practitioner/patients/${id}/overview`),
    backendFetch(`/api/practitioner/patients/${id}/get-immunizations`),
    backendFetch(`/api/practitioner/patients/${id}/get-encounters`),
    backendFetch(`/api/practitioner/patients/${id}/get-recommendations`),
    backendFetch(`/api/practitioner/patients/${id}/age-based-recommendations`),
    backendFetch(`/api/practitioner/patients/${id}/get-appointments`),
    backendFetch(`/api/practitioner/patients/${id}/get-allergies`),
    backendFetch(`/api/practitioner/patients/${id}/adverse-events`),
  ])

  const errors: string[] = []

  async function parseJson<T>(result: PromiseSettledResult<Response>, fallback: T, label: string): Promise<T> {
    if (result.status === 'rejected') { errors.push(`${label}: ${result.reason}`); return fallback }
    try { return ((await result.value.json()) as T) ?? fallback }
    catch { errors.push(`${label}: failed to parse response`); return fallback }
  }

  overview.value = await parseJson(overviewResult, null, 'overview')
  immunizations.value = await parseJson(immunizationsResult, [], 'immunizations')
  encounters.value = await parseJson(encountersResult, [], 'encounters')
  recommendations.value = await parseJson(recommendationsResult, [], 'recommendations')
  ageBasedRecommendations.value = await parseJson(ageRecsResult, [], 'age-based-recommendations')
  appointments.value = await parseJson(appointmentsResult, [], 'appointments')
  allergies.value = await parseJson(allergiesResult, [], 'allergies')
  adverseEvents.value = await parseJson(adverseResult, [], 'adverse-events')

  // Only surface errors for the critical endpoints; age-based-recommendations is optional
  const criticalErrors = errors.filter(e => !e.startsWith('age-based-recommendations'))
  if (criticalErrors.length > 0) error.value = criticalErrors.join('\n')

  loading.value = false
}

watch(
  () => props.open,
  (v) => { if (v && props.patientId) void loadAll() },
)

watch(
  () => props.patientId,
  (id, prev) => { if (props.open && id && id !== prev) void loadAll() },
)
</script>
