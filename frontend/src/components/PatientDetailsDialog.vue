<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent v-if="open" class="max-w-4xl max-h-[90vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>Patient Medical Record</DialogTitle>
        <DialogDescription>FHIR patient details and vaccination history</DialogDescription>
      </DialogHeader>

      <div class="space-y-6">
        <div class="bg-gradient-to-r from-blue-50 to-purple-50 p-6 rounded-lg space-y-3">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="w-16 h-16 bg-gradient-to-br from-blue-500 to-purple-500 rounded-full flex items-center justify-center text-white text-xl">
                {{ initials(patientDisplayName) }}
              </div>
              <div>
                <h3 class="text-gray-900">{{ patientDisplayName || 'Patient' }}</h3>
                <p class="text-gray-600">FHIR Patient ID: {{ patientId }}</p>
              </div>
            </div>
            <Badge variant="outline" class="bg-white">
              <Shield class="w-3 h-3 mr-1" />
              Verified
            </Badge>
          </div>

          <Separator />

          <div class="grid grid-cols-2 md:grid-cols-3 gap-3 text-gray-700">
            <div class="flex items-center gap-2">
              <Calendar class="w-4 h-4 text-gray-500" />
              <span>
                DOB:
                <span v-if="patientBirthDate">{{ formatDate(patientBirthDate) }}</span>
                <span v-else>Unknown</span>
                <span v-if="patientBirthDate && patientAgeLabel" class="text-gray-500">({{ patientAgeLabel }})</span>
              </span>
            </div>
            <div class="flex items-center gap-2">
              <MapPin class="w-4 h-4 text-gray-500" />
              <span>{{ patientCountry ?? 'Unknown' }}</span>
            </div>
            <div class="flex items-center gap-2">
              <User class="w-4 h-4 text-gray-500" />
              <span>Identifier: {{ patientIdentifierLabel || 'None' }}</span>
            </div>
          </div>
          <p v-if="loading" class="text-sm text-muted-foreground">Loading…</p>
          <p v-else-if="error" class="text-sm text-red-600">{{ error }}</p>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="bg-green-50 p-4 rounded-lg border border-green-200">
            <div class="text-green-600">{{ completedVaccinations.length }}</div>
            <div class="text-gray-600">Completed Vaccinations</div>
          </div>
          <div class="bg-blue-50 p-4 rounded-lg border border-blue-200">
            <div class="text-blue-600">{{ upcomingVaccinations.length }}</div>
            <div class="text-gray-600">Future-dated Records</div>
          </div>
        </div>

        <Card class="bg-white shadow-sm">
          <CardHeader class="border-b">
            <div class="flex items-start justify-between gap-4">
              <div>
                <CardTitle>Reminders</CardTitle>
                <CardDescription>
                  Suggestions based on what is recorded in this registry. Recommendations vary by country and personal risk.
                </CardDescription>
              </div>
              <div class="flex flex-col items-end gap-2">
                <div class="text-xs text-muted-foreground">Schedule profile</div>
                <select
                  v-model="scheduleProfile"
                  class="border-input bg-input-background dark:bg-input/30 flex h-9 rounded-md border px-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50"
                  aria-label="Schedule profile"
                >
                  <option v-for="p in scheduleProfiles" :key="p.key" :value="p.key">{{ p.label }}</option>
                </select>

                <label class="flex items-center gap-2 text-xs text-muted-foreground select-none">
                  <input
                    type="checkbox"
                    v-model="includeTravelOptional"
                    class="h-4 w-4 rounded border-gray-300"
                    aria-label="Show travel/optional vaccines"
                  />
                  Show travel/optional vaccines
                </label>
              </div>
            </div>
          </CardHeader>
          <CardContent class="pt-6">
            <div class="space-y-8">
              <!-- Overdue: action-oriented -->
              <div>
                <div class="flex items-center justify-between">
                  <div class="font-medium text-gray-900">Overdue</div>
                  <Badge
                    v-if="overdueReminders.length > 0"
                    variant="outline"
                    class="border-red-200 bg-red-50 text-red-700"
                  >
                    {{ overdueReminders.length }}
                  </Badge>
                </div>
                <div v-if="overdueReminders.length === 0" class="mt-2 text-sm text-gray-600">
                  No overdue vaccines detected.
                </div>

                <div v-else class="mt-3 space-y-3">
                  <div
                    v-for="r in overdueReminders"
                    :key="`overdue:${r.key}`"
                    class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                  >
                    <div class="flex items-start gap-3">
                      <div class="mt-0.5">
                        <AlertTriangle class="w-5 h-5 text-red-600" />
                      </div>
                      <div>
                        <div class="flex items-center gap-2">
                          <div class="font-medium text-gray-900">{{ r.title }}</div>
                          <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                        </div>
                        <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                        <div v-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                      </div>
                    </div>

                    <div class="flex gap-2">
                      <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Due soon -->
              <div>
                <div class="flex items-center justify-between">
                  <div class="font-medium text-gray-900">Due soon</div>
                  <div class="text-xs text-muted-foreground">Next 3 months</div>
                </div>
                <div v-if="dueSoonReminders.length === 0" class="mt-2 text-sm text-gray-600">
                  Nothing due soon.
                </div>

                <div v-else class="mt-3 space-y-3">
                  <div
                    v-for="r in dueSoonReminders"
                    :key="`soon:${r.key}`"
                    class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                  >
                    <div class="flex items-start gap-3">
                      <div class="mt-0.5">
                        <Clock class="w-5 h-5 text-orange-600" />
                      </div>
                      <div>
                        <div class="flex items-center gap-2">
                          <div class="font-medium text-gray-900">{{ r.title }}</div>
                          <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                        </div>
                        <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                        <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Due: {{ formatDate(r.nextDueDate) }}</div>
                        <div v-else-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                      </div>
                    </div>

                    <div class="flex gap-2">
                      <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Travel / optional vaccines -->
              <div v-if="showOptionalSection">
                <div class="flex items-center justify-between">
                  <div class="font-medium text-gray-900">Travel / optional</div>
                  <div class="text-xs text-muted-foreground">
                    {{ includeTravelOptional ? 'Enabled' : 'Shown because recorded' }}
                  </div>
                </div>
                <div v-if="optionalReminders.length === 0" class="mt-2 text-sm text-gray-600">
                  No travel/optional items.
                </div>

                <div v-else class="mt-3 space-y-3">
                  <div
                    v-for="r in optionalReminders"
                    :key="`opt:${r.key}`"
                    class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                  >
                    <div class="flex items-start gap-3">
                      <div class="mt-0.5">
                        <Info class="w-5 h-5 text-blue-600" />
                      </div>
                      <div>
                        <div class="flex items-center gap-2">
                          <div class="font-medium text-gray-900">{{ r.title }}</div>
                          <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                        </div>
                        <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                        <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Next: {{ formatDate(r.nextDueDate) }}</div>
                        <div v-else-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                      </div>
                    </div>

                    <div class="flex gap-2">
                      <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                    </div>
                  </div>
                </div>
              </div>

              <!-- Recommended / upcoming -->
              <div>
                <div class="flex items-center justify-between gap-3">
                  <div class="font-medium text-gray-900">Recommended</div>
                  <Button variant="outline" size="sm" @click="showAllGuidance = !showAllGuidance">
                    {{ showAllGuidance ? 'Hide' : 'Show' }}
                  </Button>
                </div>

                <div v-if="showAllGuidance" class="mt-3 space-y-6">
                  <div>
                    <div class="flex items-center justify-between">
                      <div class="font-medium text-gray-900">Recommended soon</div>
                      <div class="text-xs text-muted-foreground">Next 12 months</div>
                    </div>
                    <div v-if="recommendedSoonReminders.length === 0" class="mt-2 text-sm text-gray-600">
                      Nothing recommended soon.
                    </div>

                    <div v-else class="mt-3 space-y-3">
                      <div
                        v-for="r in recommendedSoonReminders"
                        :key="`recSoon:${r.key}`"
                        class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                      >
                        <div class="flex items-start gap-3">
                          <div class="mt-0.5">
                            <Info class="w-5 h-5 text-blue-600" />
                          </div>
                          <div>
                            <div class="flex items-center gap-2">
                              <div class="font-medium text-gray-900">{{ r.title }}</div>
                              <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                            </div>
                            <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                            <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Due: {{ formatDate(r.nextDueDate) }}</div>
                          </div>
                        </div>

                        <div class="flex gap-2">
                          <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div>
                    <div class="flex items-center justify-between">
                      <div class="font-medium text-gray-900">Upcoming</div>
                      <div class="text-xs text-muted-foreground">Beyond 12 months</div>
                    </div>
                    <div v-if="upcomingReminders.length === 0" class="mt-2 text-sm text-gray-600">
                      No upcoming items.
                    </div>

                    <div v-else class="mt-3 space-y-3">
                      <div
                        v-for="r in upcomingReminders"
                        :key="`up:${r.key}`"
                        class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                      >
                        <div class="flex items-start gap-3">
                          <div class="mt-0.5">
                            <Info class="w-5 h-5 text-blue-600" />
                          </div>
                          <div>
                            <div class="flex items-center gap-2">
                              <div class="font-medium text-gray-900">{{ r.title }}</div>
                              <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                            </div>
                            <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                            <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Planned around: {{ formatDate(r.nextDueDate) }}</div>
                          </div>
                        </div>

                        <div class="flex gap-2">
                          <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                        </div>
                      </div>
                    </div>
                  </div>

                  <div>
                    <div class="font-medium text-gray-900">General guidance</div>
                    <div v-if="otherGuidanceReminders.length === 0" class="mt-2 text-sm text-gray-600">
                      No additional guidance items.
                    </div>

                    <div v-else class="mt-3 space-y-3">
                      <div
                        v-for="r in otherGuidanceReminders"
                        :key="`other:${r.key}`"
                        class="flex flex-col md:flex-row md:items-center justify-between gap-3 rounded-lg border p-4"
                      >
                        <div class="flex items-start gap-3">
                          <div class="mt-0.5">
                            <Info class="w-5 h-5 text-blue-600" />
                          </div>
                          <div>
                            <div class="flex items-center gap-2">
                              <div class="font-medium text-gray-900">{{ r.title }}</div>
                              <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                            </div>
                            <div class="mt-1 text-sm text-gray-600">{{ r.message }}</div>
                            <div v-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                          </div>
                        </div>

                        <div class="flex gap-2">
                          <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                        </div>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
</CardContent>
        </Card>

        <VaccineInfoDialog
          v-if="selectedVaccineKey"
          v-model:open="infoOpen"
          :vaccineKey="selectedVaccineKey"
          :reminder="selectedReminder"
          :scheduleProfile="scheduleProfile"
        />

        <div class="space-y-3">
          <h4 class="text-gray-900">Vaccination History</h4>
          <div class="space-y-3">
            <VaccinationCard v-for="v in vaccinations" :key="v.id" :vaccination="v" />
            <Card v-if="!loading && vaccinations.length === 0">
              <CardContent class="pt-6 text-center text-gray-500">No immunizations found</CardContent>
            </Card>
          </div>
        </div>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { User, Calendar, MapPin, Shield, Clock, CheckCircle2, Info, AlertTriangle } from 'lucide-vue-next'

import type { Vaccination } from '@/mockData'
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

import VaccinationCard from '@/components/VaccinationCard.vue'
import VaccineInfoDialog from '@/components/VaccineInfoDialog.vue'

import type { VaccineReminder, ReminderStatus } from '@/vaccines/reminders'
import { computeVaccineReminders } from '@/vaccines/reminders'
import type { VaccineKey } from '@/vaccines/vaccineCatalog'
import {
  scheduleProfiles,
  inferProfileFromCountry,
  readScheduleProfileFromStorage,
  writeScheduleProfileToStorage,
  type VaccineScheduleProfile,
} from '@/vaccines/scheduleProfiles'

const props = defineProps<{ patientId: string; open: boolean }>()
const emit = defineEmits<{ (e: 'update:open', v: boolean): void }>()

const patientResource = ref<any | null>(null)
const vaccinations = ref<Vaccination[]>([])
const loading = ref(false)
const error = ref<string | null>(null)

const includeTravelOptional = ref(false)

const OPTIONAL_STORAGE_KEY_PREFIX = 'vax_registry_optional:'

function readOptionalFromStorage(patientId?: string | null): boolean {
  try {
    const key = `${OPTIONAL_STORAGE_KEY_PREFIX}${patientId ?? 'global'}`
    return window.localStorage.getItem(key) === '1'
  } catch {
    return false
  }
}

function writeOptionalToStorage(patientId: string | null | undefined, value: boolean) {
  try {
    const key = `${OPTIONAL_STORAGE_KEY_PREFIX}${patientId ?? 'global'}`
    window.localStorage.setItem(key, value ? '1' : '0')
  } catch {
    // ignore
  }
}

function initials(display: string) {
  const parts = (display || '').trim().split(/\s+/).filter(Boolean)
  if (parts.length === 0) return '?'
  if (parts.length === 1) return parts[0].slice(0, 2).toUpperCase()
  return (parts[0][0] + parts[parts.length - 1][0]).toUpperCase()
}

function pickDisplayName(p: any | null): string {
  const n = p?.name?.[0]
  if (!n) return ''
  if (typeof n.text === 'string' && n.text.trim()) return n.text.trim()
  const given = Array.isArray(n.given) ? n.given.join(' ') : ''
  const family = typeof n.family === 'string' ? n.family : ''
  return `${given} ${family}`.trim()
}

function pickBirthDate(p: any | null): string | null {
  const bd = p?.birthDate
  return typeof bd === 'string' && bd.trim() ? bd : null
}

function pickIdentifierLabel(p: any | null): string {
  const ids = Array.isArray(p?.identifier) ? p.identifier : []
  const first = ids.find((i: any) => typeof i?.value === 'string' && i.value.trim())
  if (first?.system && first?.value) return `${first.system}|${first.value}`
  if (first?.value) return String(first.value)
  return ''
}

function pickCountry(p: any | null): string | null {
  const addrs = Array.isArray(p?.address) ? p.address : []
  const c = addrs.find((a: any) => typeof a?.country === 'string' && a.country.trim())?.country
  return typeof c === 'string' && c.trim() ? c.trim() : null
}

function toVaccination(immunization: any): Vaccination {
  const code = immunization?.vaccineCode
  const codings = Array.isArray(code?.coding) ? code.coding : []
  const cvx = codings.find((c: any) => c?.system === 'http://hl7.org/fhir/sid/cvx') ?? codings[0] ?? null
  const vaccineName = (cvx?.display || code?.text || 'Unknown').toString()
  const vaccineType = (cvx?.system || 'Immunization').toString()
  const vaccineSystem = typeof cvx?.system === 'string' ? cvx.system : undefined
  const vaccineCode = typeof cvx?.code === 'string' ? cvx.code : undefined

  const occurrence = immunization?.occurrenceDateTime || immunization?.occurrenceString || immunization?.recorded
  const date = typeof occurrence === 'string' && occurrence.trim() ? occurrence : new Date().toISOString().slice(0, 10)

  const pa0 = Array.isArray(immunization?.protocolApplied) ? immunization.protocolApplied[0] : null
  const doseNumber = Number(pa0?.doseNumberPositiveInt ?? 1)
  const totalDoses = Number(pa0?.seriesDosesPositiveInt ?? doseNumber)

  const manufacturer = immunization?.manufacturer?.display || immunization?.manufacturer?.reference || 'Unknown'
  const batchNumber = immunization?.lotNumber || 'Unknown'

  const performer0 = Array.isArray(immunization?.performer) ? immunization.performer[0] : null
  const administeredBy = performer0?.actor?.display || performer0?.actor?.reference || 'Unknown'
  const location = immunization?.location?.display || immunization?.location?.reference || 'Unknown'

  const status = String(immunization?.status || '').toLowerCase() === 'completed' ? 'completed' : 'scheduled'

  const d = new Date(date)
  const now = new Date()
  const adjustedStatus = d.getTime() > now.getTime() ? 'scheduled' : status

  return {
    id: String(immunization?.id || Math.random().toString(36).slice(2)),
    vaccineName,
    vaccineType,
    vaccineSystem,
    vaccineCode,
    date: new Date(date).toISOString().slice(0, 10),
    doseNumber: Number.isFinite(doseNumber) ? doseNumber : 1,
    totalDoses: Number.isFinite(totalDoses) && totalDoses > 0 ? totalDoses : 1,
    manufacturer: String(manufacturer),
    batchNumber: String(batchNumber),
    administeredBy: String(administeredBy).replace(/^Practitioner\//, ''),
    location: String(location),
    status: adjustedStatus as 'completed' | 'scheduled',
  }
}

async function loadAll() {
  const id = props.patientId.trim()
  if (!id) return
  error.value = null
  loading.value = true
  try {
    const pRes = await backendFetch(`/Patient/${encodeURIComponent(id)}`)
    patientResource.value = await pRes.json()

    const iRes = await backendFetch(`/api/patient/${encodeURIComponent(id)}/immunizations`)
    const bundle = await iRes.json()
    const entries = Array.isArray(bundle?.entry) ? bundle.entry : []
    vaccinations.value = entries
      .map((e: any) => e?.resource)
      .filter((r: any) => r && r.resourceType === 'Immunization')
      .map(toVaccination)
      .sort((a: Vaccination, b: Vaccination) => (a.date < b.date ? 1 : -1))
  } catch (e) {
    vaccinations.value = []
    patientResource.value = null
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

watch(
  () => props.open,
  (isOpen) => {
    if (isOpen) {
      void loadAll()
    }
  },
)

watch(
  () => props.patientId,
  () => {
    if (props.open) {
      void loadAll()
    }
  },
)

const patientDisplayName = computed(() => pickDisplayName(patientResource.value))
const patientBirthDate = computed(() => pickBirthDate(patientResource.value))
const patientCountry = computed(() => pickCountry(patientResource.value))
const patientIdentifierLabel = computed(() => pickIdentifierLabel(patientResource.value))

function ageLabelFromBirthDate(birthDateIso: string | null | undefined): string {
  if (!birthDateIso) return ''
  const birth = new Date(birthDateIso)
  const now = new Date()
  if (Number.isNaN(birth.getTime())) return ''

  let months = (now.getFullYear() - birth.getFullYear()) * 12 + (now.getMonth() - birth.getMonth())
  if (now.getDate() < birth.getDate()) months -= 1
  if (months < 0) months = 0

  if (months < 24) return `${months} months`
  const years = Math.floor(months / 12)
  return `${years} years`
}

const patientAgeLabel = computed(() => ageLabelFromBirthDate(patientBirthDate.value))

const scheduleProfile = ref<VaccineScheduleProfile>('GLOBAL')

watch(
  () => patientCountry.value,
  (c) => {
    const stored = readScheduleProfileFromStorage(props.patientId)
    scheduleProfile.value = stored ?? inferProfileFromCountry(c)
  },
  { immediate: true },
)

watch(
  () => scheduleProfile.value,
  (p) => {
    writeScheduleProfileToStorage(props.patientId, p)
  },
)

watch(
  () => props.patientId,
  (pid) => {
    includeTravelOptional.value = readOptionalFromStorage(pid)
  },
  { immediate: true },
)

watch(
  () => includeTravelOptional.value,
  (v) => {
    writeOptionalToStorage(props.patientId, v)
  },
)

const completedVaccinations = computed(() => vaccinations.value.filter((v) => v.status === 'completed'))
const upcomingVaccinations = computed(() => vaccinations.value.filter((v) => v.status === 'scheduled'))

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB')
}

const vaccineReminders = computed(() =>
  computeVaccineReminders({
    vaccinations: vaccinations.value,
    patientBirthDate: patientBirthDate.value,
    patientCountry: patientCountry.value,
    scheduleProfile: scheduleProfile.value,
    includeTravelOptional: includeTravelOptional.value,
  }),
)

const showAllGuidance = ref(false)

const OPTIONAL_VACCINE_KEYS: VaccineKey[] = ['HEPA']
function isOptionalVaccine(key: VaccineKey): boolean {
  return OPTIONAL_VACCINE_KEYS.includes(key)
}

function isWithinDays(iso: string | null | undefined, days: number): boolean {
  if (!iso) return false
  const dt = new Date(iso)
  if (Number.isNaN(dt.getTime())) return false
  const now = new Date()
  const diffMs = dt.getTime() - now.getTime()
  const diffDays = diffMs / (1000 * 60 * 60 * 24)
  return diffDays >= 0 && diffDays <= days
}

const routineReminders = computed(() =>
  vaccineReminders.value.filter((r) => r.status !== 'up-to-date' && !isOptionalVaccine(r.key)),
)

const overdueReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'due' || r.status === 'missing'),
)

const dueSoonReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'due-soon' && isWithinDays(r.nextDueDate, 90)),
)

const recommendedSoonReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'due-soon' && !isWithinDays(r.nextDueDate, 90) && isWithinDays(r.nextDueDate, 365)),
)

const upcomingReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'due-soon' && r.nextDueDate && !isWithinDays(r.nextDueDate, 365)),
)

const otherGuidanceReminders = computed(() => routineReminders.value.filter((r) => r.status === 'unknown'))

const optionalContinuationReminders = computed(() =>
  vaccineReminders.value.filter((r) => {
    if (!isOptionalVaccine(r.key)) return false
    if (typeof r.dosesRecorded !== 'number' || typeof r.seriesTargetDoses !== 'number') return false
    return r.dosesRecorded > 0 && r.dosesRecorded < r.seriesTargetDoses
  }),
)

const optionalReminders = computed(() => {
  if (includeTravelOptional.value) {
    return vaccineReminders.value.filter((r) =>
      isOptionalVaccine(r.key) && (r.status !== 'up-to-date' || optionalContinuationReminders.value.some((x) => x.key === r.key)),
    )
  }
  return optionalContinuationReminders.value
})

const showOptionalSection = computed(() => includeTravelOptional.value || optionalReminders.value.length > 0)

const infoOpen = ref(false)
const selectedVaccineKey = ref<VaccineKey | null>(null)
const selectedReminder = ref<VaccineReminder | null>(null)

function openVaccineInfo(r: VaccineReminder) {
  selectedVaccineKey.value = r.key
  selectedReminder.value = r
  infoOpen.value = true
}

function statusLabel(s: ReminderStatus): string {
  switch (s) {
    case 'due':
      return 'Due'
    case 'due-soon':
      return 'Due soon'
    case 'missing':
      return 'Missing'
    case 'up-to-date':
      return 'Up to date'
    default:
      return 'Check'
  }
}

function statusBadgeClass(s: ReminderStatus): string {
  if (s === 'due') return 'border-red-200 bg-red-50 text-red-700'
  if (s === 'due-soon') return 'border-orange-200 bg-orange-50 text-orange-700'
  if (s === 'missing') return 'border-gray-200 bg-gray-50 text-gray-700'
  if (s === 'up-to-date') return 'border-green-200 bg-green-50 text-green-700'
  return 'border-blue-200 bg-blue-50 text-blue-700'
}
</script>
