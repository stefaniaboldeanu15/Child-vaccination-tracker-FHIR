<template>
  <Dialog :open="open" @update:open="$emit('update:open', $event)">
    <DialogContent class="max-w-2xl max-h-[90vh] overflow-y-auto">
      <DialogHeader>
        <DialogTitle>{{ info.label }}</DialogTitle>
        <DialogDescription>
          {{ info.short }}
        </DialogDescription>
      </DialogHeader>

      <div class="mt-4 space-y-4 text-sm text-gray-700">
        <div v-if="reminder" class="rounded-lg border bg-muted/30 p-3">
          <div class="flex items-center justify-between gap-2">
            <div class="font-medium">Your record</div>
            <Badge variant="outline" :class="statusBadgeClass(reminder.status)">{{ statusLabel(reminder.status) }}</Badge>
          </div>
          <div class="mt-2 text-gray-600">
            <div v-if="reminder.lastDoseDate">Last recorded dose: {{ formatDate(reminder.lastDoseDate) }}</div>
            <div v-if="typeof reminder.dosesRecorded === 'number'">Recorded doses: {{ reminder.dosesRecorded }}</div>
            <div class="mt-2">{{ reminder.message }}</div>
          </div>
        </div>

        <div>
          <div class="font-medium">What it protects against</div>
          <div class="mt-1 text-gray-600">{{ info.protectsAgainst }}</div>
        </div>

        <div>
          <div class="font-medium">Who it’s typically for</div>
          <div class="mt-1 text-gray-600">{{ info.typicalUse }}</div>
        </div>

        <div>
          <div class="font-medium">Schedule / boosters</div>
          <div class="mt-1 text-gray-600">{{ scheduleText }}</div>
          <div v-if="reminderScheduleLine" class="mt-2 text-gray-600">
            {{ reminderScheduleLine }}
          </div>
        </div>

        <div v-if="info.costNotes">
          <div class="font-medium">Costs / coverage (best‑effort)</div>
          <div class="mt-1 text-gray-600">{{ info.costNotes }}</div>
        </div>

        <div v-if="scheduleProfileInfo" class="rounded-lg border bg-muted/20 p-3">
          <div class="font-medium">Schedule profile</div>
          <div class="mt-1 text-gray-600">
            {{ scheduleProfileInfo.label }} — {{ scheduleProfileInfo.description }}
          </div>
        </div>

        <div v-if="info.codings && info.codings.length" class="rounded-lg border p-3">
          <div class="font-medium">Standard codes (for matching)</div>
          <ul class="mt-1 list-disc pl-5 text-gray-600">
            <li v-for="c in info.codings" :key="`${c.system}|${c.code}`" class="break-words">
              <span class="font-mono break-all">{{ c.system }}</span> — <span class="font-mono break-all">{{ c.code }}</span>
              <span v-if="c.display"> ({{ c.display }})</span>
            </li>
          </ul>
        </div>

        <div v-if="referenceLinks.length" class="rounded-lg border p-3">
          <div class="font-medium">References</div>
          <ul class="mt-1 list-disc pl-5 text-gray-600">
            <li v-for="s in referenceLinks" :key="s.url">
              <a :href="s.url" target="_blank" rel="noreferrer" class="underline break-words">{{ s.label }}</a>
              <span v-if="s.note"> — {{ s.note }}</span>
            </li>
          </ul>
        </div>

        <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div>
            <div class="font-medium">Common side effects</div>
            <ul class="mt-1 list-disc pl-5 text-gray-600">
              <li v-for="s in info.commonSideEffects" :key="s">{{ s }}</li>
            </ul>
          </div>
          <div>
            <div class="font-medium">Rare serious reactions</div>
            <ul class="mt-1 list-disc pl-5 text-gray-600">
              <li v-for="s in info.rareSeriousSideEffects" :key="s">{{ s }}</li>
            </ul>
          </div>
        </div>

        <div class="rounded-lg border p-3 text-xs text-gray-600">
          This section provides general information for demonstration purposes only. Vaccination recommendations and booster intervals vary by country, age, vaccine product, and medical history. For personal advice, consult a qualified healthcare professional.
        </div>
      </div>

      <div class="mt-6 flex justify-end">
        <Button variant="outline" @click="$emit('update:open', false)">Close</Button>
      </div>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import Button from '@/components/ui/Button.vue'
import Badge from '@/components/ui/Badge.vue'
import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'

import type { VaccineKey } from '@/vaccines/vaccineCatalog'
import { getVaccineInfo } from '@/vaccines/vaccineCatalog'
import type { ReminderStatus, VaccineReminder } from '@/vaccines/reminders'
import { scheduleProfiles, type VaccineScheduleProfile } from '@/vaccines/scheduleProfiles'

const props = defineProps<{
  open: boolean
  vaccineKey: VaccineKey
  reminder?: VaccineReminder | null
  scheduleProfile?: VaccineScheduleProfile
}>()
defineEmits<{ (e: 'update:open', v: boolean): void }>()

const info = computed(() => getVaccineInfo(props.vaccineKey))

const scheduleProfileInfo = computed(() =>
  scheduleProfiles.find((p) => p.key === (props.scheduleProfile ?? 'GLOBAL')) ?? null,
)

const scheduleText = computed(() => {
  const key = props.vaccineKey
  const profile = props.scheduleProfile ?? props.reminder?.scheduleProfile ?? 'GLOBAL'
  const age = props.reminder?.patientAgeYears ?? null

  if (key === 'TETANUS') {
    if (profile === 'AUSTRIA') {
      return typeof age === 'number' && age >= 60
        ? 'Austria (Impfplan) best‑effort: tetanus boosters are commonly advised about every 5 years for people aged 60+.'
        : 'Austria (Impfplan) best‑effort: tetanus boosters are commonly advised about every 10 years (5 years for people aged 60+).'
    }
    return 'Generic: tetanus boosters are commonly advised about every ~10 years (varies by country and situation).'
  }

  if (key === 'TBE') {
    if (profile === 'AUSTRIA') {
      return typeof age === 'number' && age >= 60
        ? 'Austria (Impfplan) best‑effort: after completing the primary series, FSME/TBE boosters are often around every 3 years for people aged 60+.'
        : 'Austria (Impfplan) best‑effort: after completing the primary series, FSME/TBE boosters are often around every 5 years (3 years for people aged 60+).'
    }
    return 'Generic: after a primary series, FSME/TBE booster intervals are often in the 3–5 year range (varies by country and product).'
  }

  // Other vaccines: keep general catalogue note.
  return info.value.scheduleNotes
})

const reminderScheduleLine = computed(() => {
  const r = props.reminder
  if (!r) return null

  const parts: string[] = []
  if (typeof r.nextDoseNumber === 'number' && typeof r.seriesTargetDoses === 'number') {
    parts.push(`Series: dose ${r.nextDoseNumber} of ${r.seriesTargetDoses}`)
  }
  if (r.lastDoseDate) parts.push(`Last recorded: ${formatDate(r.lastDoseDate)}`)
  if (r.earliestDueDate) parts.push(`Earliest: ${formatDate(r.earliestDueDate)}`)
  if (r.nextDueDate) parts.push(`Next due: ${formatDate(r.nextDueDate)}`)
  if (!parts.length) return null
  return parts.join(' · ')
})

const referenceLinks = computed(() => {
  const seen = new Set<string>()
  const list: { label: string; url: string; note?: string }[] = []
  for (const s of info.value.sources ?? []) {
    if (s?.url && !seen.has(s.url)) {
      seen.add(s.url)
      list.push({ label: s.label, url: s.url, note: s.note })
    }
  }
  const p = scheduleProfileInfo.value
  for (const r of p?.references ?? []) {
    if (r?.url && !seen.has(r.url)) {
      seen.add(r.url)
      list.push({ label: r.label, url: r.url })
    }
  }
  return list
})

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('en-GB')
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
  // Badge only supports default/outline; use outline with custom colors.
  if (s === 'due') return 'border-red-200 bg-red-50 text-red-700'
  if (s === 'due-soon') return 'border-orange-200 bg-orange-50 text-orange-700'
  if (s === 'missing') return 'border-gray-200 bg-gray-50 text-gray-700'
  if (s === 'up-to-date') return 'border-green-200 bg-green-50 text-green-700'
  return 'border-blue-200 bg-blue-50 text-blue-700'
}
</script>
