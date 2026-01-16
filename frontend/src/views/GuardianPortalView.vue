<script setup lang="ts">
import { computed, onMounted, ref, watch } from 'vue'
import { AlertTriangle, Calendar, Clock, Info, UserRound } from 'lucide-vue-next'

import { backendFetch } from '@/api/backend'
import { useAuth } from '@/auth/auth'
import GuardianVaccinationCard from '@/components/GuardianVaccinationCard.vue'
import VaccineInfoDialog from '@/components/VaccineInfoDialog.vue'

import Card from '@/components/ui/Card.vue'
import CardContent from '@/components/ui/CardContent.vue'
import CardHeader from '@/components/ui/CardHeader.vue'
import CardTitle from '@/components/ui/CardTitle.vue'
import CardDescription from '@/components/ui/CardDescription.vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'

import type { VaccineReminder, ReminderStatus, VaccinationLike } from '@/vaccines/reminders'
import { computeVaccineReminders } from '@/vaccines/reminders'
import type { VaccineKey } from '@/vaccines/vaccineCatalog'
import {
  scheduleProfiles,
  readScheduleProfileFromStorage,
  writeScheduleProfileToStorage,
  type VaccineScheduleProfile,
} from '@/vaccines/scheduleProfiles'

type Child = {
  patientId: string
  firstName?: string | null
  lastName?: string | null
  birthDate?: string | null
  svnr?: string | null
}

type Immunization = {
  immunizationId: string
  vaccineDisplay?: string | null
  vaccineCode?: string | null
  occurrenceDateTime?: string | null
  status?: string | null
}

const { state } = useAuth()

const children = ref<Child[]>([])
const selectedChild = ref<Child | null>(null)
const immunizations = ref<Immunization[]>([])
const error = ref<string | null>(null)
const loading = ref(false)

const scheduleProfile = ref<VaccineScheduleProfile>('AUSTRIA')
const includeTravelOptional = ref(false)

const showAllGuidance = ref(false)

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

function childName(c: Child): string {
  return `${c.firstName ?? ''} ${c.lastName ?? ''}`.trim() || 'Child'
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB')
}

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

onMounted(async () => {
  if (!state.guardianId) return
  await loadChildren()
})

async function loadChildren() {
  error.value = null
  loading.value = true
  try {
    const res = await backendFetch(`/api/guardian/${encodeURIComponent(state.guardianId as string)}/children`)
    children.value = await res.json()
  } catch (e) {
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

async function selectChild(c: Child) {
  selectedChild.value = c
  error.value = null
  loading.value = true
  try {
    // Apply per-child UI prefs.
    scheduleProfile.value = readScheduleProfileFromStorage(c.patientId) ?? 'AUSTRIA'
    includeTravelOptional.value = readOptionalFromStorage(c.patientId)

    const res = await backendFetch(
      `/api/guardian/${encodeURIComponent(state.guardianId as string)}/children/${encodeURIComponent(c.patientId)}/immunizations`,
    )
    const list = (await res.json()) as Immunization[]
    immunizations.value = [...(Array.isArray(list) ? list : [])].sort((a, b) =>
      String(a.occurrenceDateTime || '') < String(b.occurrenceDateTime || '') ? 1 : -1,
    )
  } catch (e) {
    immunizations.value = []
    error.value = String(e)
  } finally {
    loading.value = false
  }
}

watch(
  () => scheduleProfile.value,
  (p) => {
    if (!selectedChild.value?.patientId) return
    writeScheduleProfileToStorage(selectedChild.value.patientId, p)
  },
)

watch(
  () => includeTravelOptional.value,
  (v) => {
    if (!selectedChild.value?.patientId) return
    writeOptionalToStorage(selectedChild.value.patientId, v)
  },
)

const vaccinationLikes = computed<VaccinationLike[]>(() =>
  immunizations.value.map((i) => ({
    vaccineName: i.vaccineDisplay ?? undefined,
    vaccineType: undefined,
    vaccineSystem: undefined,
    vaccineCode: i.vaccineCode ?? undefined,
    date: i.occurrenceDateTime ?? undefined,
    status: String(i.status || '').toLowerCase() === 'completed' ? 'completed' : String(i.status || 'completed'),
  })),
)

const childBirthDate = computed(() => selectedChild.value?.birthDate ?? null)

const vaccineReminders = computed(() =>
  computeVaccineReminders({
    vaccinations: vaccinationLikes.value,
    patientBirthDate: childBirthDate.value,
    patientCountry: 'AT',
    scheduleProfile: scheduleProfile.value,
    includeTravelOptional: includeTravelOptional.value,
  }),
)

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

const overdueReminders = computed(() => routineReminders.value.filter((r) => r.status === 'due' || r.status === 'missing'))

const dueSoonReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'due-soon' && isWithinDays(r.nextDueDate, 90)),
)

const recommendedSoonReminders = computed(() =>
  routineReminders.value.filter(
    (r) => r.status === 'due-soon' && !!r.nextDueDate && !isWithinDays(r.nextDueDate, 90) && isWithinDays(r.nextDueDate, 365),
  ),
)

const upcomingRecommendedReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'due-soon' && !!r.nextDueDate && !isWithinDays(r.nextDueDate, 365)),
)

const otherGuidanceReminders = computed(() =>
  routineReminders.value.filter((r) => r.status === 'unknown' || (r.status === 'due-soon' && !r.nextDueDate)),
)

const optionalContinuationReminders = computed(() =>
  vaccineReminders.value.filter((r) => {
    if (!isOptionalVaccine(r.key)) return false
    const recorded = typeof r.dosesRecorded === 'number' ? r.dosesRecorded : 0
    const target = typeof r.seriesTargetDoses === 'number' ? r.seriesTargetDoses : 0
    return recorded > 0 && target > 0 && recorded < target
  }),
)

const optionalReminders = computed(() => {
  const continuation = optionalContinuationReminders.value
  if (!includeTravelOptional.value) return continuation

  const enabled = vaccineReminders.value.filter((r) => isOptionalVaccine(r.key) && r.status !== 'up-to-date')
  const dedup = new Map<VaccineKey, (typeof enabled)[number]>()
  for (const r of [...enabled, ...continuation]) dedup.set(r.key, r)
  return Array.from(dedup.values())
})

const showOptionalSection = computed(() => includeTravelOptional.value || optionalContinuationReminders.value.length > 0)

const completedImmunizations = computed(() =>
  immunizations.value.filter((i) => String(i.status || '').toLowerCase() === 'completed'),
)

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
      return 'Upcoming'
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
  if (s === 'due-soon') return 'border-blue-200 bg-blue-50 text-blue-700'
  if (s === 'missing') return 'border-gray-200 bg-gray-50 text-gray-700'
  if (s === 'up-to-date') return 'border-green-200 bg-green-50 text-green-700'
  return 'border-blue-200 bg-blue-50 text-blue-700'
}
</script>

<template>
  <div class="max-w-6xl mx-auto space-y-6">
    <div>
      <h1 class="text-3xl text-gray-900">My Children's Vaccination Records</h1>
      <p class="mt-1 text-gray-600">Track your children's immunization history and see upcoming or overdue items.</p>
    </div>

    <div v-if="error" class="rounded-md bg-red-50 border border-red-200 p-3 text-sm text-red-700">
      {{ error }}
    </div>

    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <button
        v-for="c in children"
        :key="c.patientId"
        class="text-left"
        @click="selectChild(c)"
      >
        <Card
          :class="
            selectedChild?.patientId === c.patientId
              ? 'border-blue-400 ring-2 ring-blue-100 shadow-sm'
              : 'border-gray-200 hover:border-gray-300 hover:shadow-sm'
          "
        >
          <CardContent class="pt-6">
            <div class="flex items-start gap-3">
              <div class="w-11 h-11 rounded-full bg-gradient-to-br from-blue-500 to-purple-500 flex items-center justify-center">
                <UserRound class="w-5 h-5 text-white" />
              </div>
              <div class="min-w-0">
                <div class="text-lg font-medium text-gray-900 break-words">{{ childName(c) }}</div>
                <div class="mt-1 text-sm text-gray-600">
                  Born:
                  <span v-if="c.birthDate">{{ formatDate(c.birthDate) }}</span>
                  <span v-else>Unknown</span>
                  <span v-if="c.birthDate" class="text-gray-500">({{ ageLabelFromBirthDate(c.birthDate) }})</span>
                </div>
                <div class="mt-2 text-xs text-gray-500">
                  Patient ID: {{ c.patientId }}
                  <span v-if="c.svnr"> · SVNR: {{ c.svnr }}</span>
                </div>
              </div>
            </div>
          </CardContent>
        </Card>
      </button>
    </div>

    <div v-if="loading" class="text-sm text-gray-600">Loading…</div>

    <div v-if="selectedChild" class="grid grid-cols-1 lg:grid-cols-2 gap-4">
      <!-- Upcoming & due -->
      <Card>
        <CardHeader class="border-b">
          <div class="flex items-start justify-between gap-4">
            <div>
              <CardTitle class="flex items-center gap-2">
                <Clock class="w-4 h-4 text-blue-600" />
                Upcoming & Due Vaccinations
              </CardTitle>
              <CardDescription>
                Vaccines that appear due or upcoming based on what is recorded for {{ childName(selectedChild) }}.
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
                Show travel/optional
              </label>
            </div>
          </div>
        </CardHeader>
        <CardContent class="pt-6">
          <div class="space-y-6">
            <div>
              <div class="flex items-center justify-between">
                <div class="font-medium text-gray-900">Overdue</div>
                <Badge v-if="overdueReminders.length" variant="outline" class="border-red-200 bg-red-50 text-red-700">
                  {{ overdueReminders.length }}
                </Badge>
              </div>
              <div v-if="overdueReminders.length === 0" class="mt-2 text-sm text-gray-600">No overdue vaccines detected.</div>
              <div v-else class="mt-3 space-y-3">
                <div
                  v-for="r in overdueReminders"
                  :key="`overdue:${r.key}`"
                  class="flex items-start justify-between gap-3 rounded-lg border p-4"
                >
                  <div class="flex items-start gap-3">
                    <AlertTriangle class="w-5 h-5 text-red-600 mt-0.5" />
                    <div>
                      <div class="flex items-center gap-2">
                        <div class="font-medium text-gray-900">{{ r.title }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                      </div>
                      <div v-if="r.recommendedAgeLabel" class="mt-1 text-xs text-gray-500">
                        Recommended: {{ r.recommendedAgeLabel }}
                      </div>
                      <div v-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                    </div>
                  </div>
                  <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                </div>
              </div>
            </div>

            <div>
              <div class="flex items-center justify-between">
                <div class="font-medium text-gray-900">Upcoming</div>
                <div class="text-xs text-muted-foreground">Next 3 months</div>
              </div>
              <div v-if="dueSoonReminders.length === 0" class="mt-2 text-sm text-gray-600">Nothing upcoming soon.</div>
              <div v-else class="mt-3 space-y-3">
                <div
                  v-for="r in dueSoonReminders"
                  :key="`soon:${r.key}`"
                  class="flex items-start justify-between gap-3 rounded-lg border p-4"
                >
                  <div class="flex items-start gap-3">
                    <Clock class="w-5 h-5 text-blue-600 mt-0.5" />
                    <div>
                      <div class="flex items-center gap-2">
                        <div class="font-medium text-gray-900">{{ r.title }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                      </div>
                      <div v-if="r.recommendedAgeLabel" class="mt-1 text-xs text-gray-500">
                        Recommended: {{ r.recommendedAgeLabel }}
                      </div>
                      <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Due: {{ formatDate(r.nextDueDate) }}</div>
                    </div>
                  </div>
                  <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                </div>
              </div>
            </div>

            <div v-if="showOptionalSection">
              <div class="flex items-center justify-between">
                <div class="font-medium text-gray-900">Travel / optional</div>
                <div class="text-xs text-muted-foreground">{{ includeTravelOptional ? 'Enabled' : 'Shown because recorded' }}</div>
              </div>
              <div v-if="optionalReminders.length === 0" class="mt-2 text-sm text-gray-600">No travel/optional items.</div>
              <div v-else class="mt-3 space-y-3">
                <div
                  v-for="r in optionalReminders"
                  :key="`opt:${r.key}`"
                  class="flex items-start justify-between gap-3 rounded-lg border p-4"
                >
                  <div class="flex items-start gap-3">
                    <Info class="w-5 h-5 text-blue-600 mt-0.5" />
                    <div>
                      <div class="flex items-center gap-2">
                        <div class="font-medium text-gray-900">{{ r.title }}</div>
                        <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                      </div>
                      <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Next: {{ formatDate(r.nextDueDate) }}</div>
                      <div v-else-if="r.lastDoseDate" class="mt-1 text-xs text-gray-500">Last recorded: {{ formatDate(r.lastDoseDate) }}</div>
                    </div>
                  </div>
                  <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                </div>
              </div>
            </div>

            <div>
              <div class="flex items-center justify-between gap-3">
                <div class="font-medium text-gray-900">Recommendations</div>
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
                  <div v-if="recommendedSoonReminders.length === 0" class="mt-2 text-sm text-gray-600">Nothing recommended soon.</div>
                  <div v-else class="mt-3 space-y-3">
                    <div
                      v-for="r in recommendedSoonReminders"
                      :key="`recSoon:${r.key}`"
                      class="flex items-start justify-between gap-3 rounded-lg border p-4"
                    >
                      <div class="flex items-start gap-3">
                        <Info class="w-5 h-5 text-blue-600 mt-0.5" />
                        <div>
                          <div class="flex items-center gap-2">
                            <div class="font-medium text-gray-900">{{ r.title }}</div>
                            <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                          </div>
                          <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Planned: {{ formatDate(r.nextDueDate) }}</div>
                        </div>
                      </div>
                      <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                    </div>
                  </div>
                </div>

                <div>
                  <div class="flex items-center justify-between">
                    <div class="font-medium text-gray-900">Upcoming (later)</div>
                    <div class="text-xs text-muted-foreground">Beyond 12 months</div>
                  </div>
                  <div v-if="upcomingRecommendedReminders.length === 0" class="mt-2 text-sm text-gray-600">No upcoming items.</div>
                  <div v-else class="mt-3 space-y-3">
                    <div
                      v-for="r in upcomingRecommendedReminders"
                      :key="`up:${r.key}`"
                      class="flex items-start justify-between gap-3 rounded-lg border p-4"
                    >
                      <div class="flex items-start gap-3">
                        <Info class="w-5 h-5 text-blue-600 mt-0.5" />
                        <div>
                          <div class="flex items-center gap-2">
                            <div class="font-medium text-gray-900">{{ r.title }}</div>
                            <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                          </div>
                          <div v-if="r.nextDueDate" class="mt-1 text-xs text-gray-500">Planned around: {{ formatDate(r.nextDueDate) }}</div>
                        </div>
                      </div>
                      <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                    </div>
                  </div>
                </div>

                <div>
                  <div class="font-medium text-gray-900">Other guidance</div>
                  <div v-if="otherGuidanceReminders.length === 0" class="mt-2 text-sm text-gray-600">No additional guidance items.</div>
                  <div v-else class="mt-3 space-y-3">
                    <div
                      v-for="r in otherGuidanceReminders"
                      :key="`other:${r.key}`"
                      class="flex items-start justify-between gap-3 rounded-lg border p-4"
                    >
                      <div class="flex items-start gap-3">
                        <Info class="w-5 h-5 text-blue-600 mt-0.5" />
                        <div>
                          <div class="flex items-center gap-2">
                            <div class="font-medium text-gray-900">{{ r.title }}</div>
                            <Badge variant="outline" :class="statusBadgeClass(r.status)">{{ statusLabel(r.status) }}</Badge>
                          </div>
                        </div>
                      </div>
                      <Button variant="outline" size="sm" @click="openVaccineInfo(r)">Details</Button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <!-- History -->
      <Card>
        <CardHeader class="border-b">
          <CardTitle class="flex items-center gap-2">
            <Calendar class="w-4 h-4 text-green-600" />
            Vaccination History
          </CardTitle>
          <CardDescription>Completed vaccinations for {{ childName(selectedChild) }}.</CardDescription>
        </CardHeader>
        <CardContent class="pt-6">
          <div v-if="completedImmunizations.length === 0" class="text-sm text-gray-600">No completed vaccinations found.</div>
          <div v-else class="max-h-[520px] overflow-y-auto pr-2 space-y-3">
            <GuardianVaccinationCard
              v-for="i in completedImmunizations"
              :key="i.immunizationId"
              :immunization="i"
            />
          </div>
        </CardContent>
      </Card>
    </div>

    <VaccineInfoDialog
      v-if="selectedVaccineKey"
      v-model:open="infoOpen"
      :vaccineKey="selectedVaccineKey"
      :reminder="selectedReminder"
      :scheduleProfile="scheduleProfile"
    />
  </div>
</template>
