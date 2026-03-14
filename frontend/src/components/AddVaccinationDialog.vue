<template>
  <Dialog :open="open" @update:open="(v) => emit('update:open', v)">
    <DialogContent class="max-w-2xl">
      <DialogHeader>
        <DialogTitle>Add Vaccination Record</DialogTitle>
        <DialogDescription>
          Create an Immunization for FHIR Patient/{{ patientId }}
        </DialogDescription>
      </DialogHeader>

      <form @submit.prevent="handleSubmit" class="space-y-4">
        <div class="space-y-2">
          <Label for-id="catalogVaccine">Vaccine (recommended)</Label>
          <select
            id="catalogVaccine"
            v-model="selectedCatalog"
            class="border-input data-[placeholder]:text-muted-foreground focus-visible:border-ring focus-visible:ring-ring/50 dark:bg-input/30 flex w-full items-center justify-between gap-2 rounded-md border bg-input-background px-3 py-2 text-sm transition-[color,box-shadow] outline-none focus-visible:ring-[3px]"
          >
            <option disabled value="">Select vaccine</option>
            <option v-for="o in catalogOptions" :key="o.value" :value="o.value">{{ o.label }}</option>
            <option value="custom">Custom</option>
          </select>
          <p class="text-xs text-muted-foreground">
            This autofills standard CVX coding when available. Use “Custom” only when you need a non-standard code.
          </p>
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for-id="vaccineSystem">Vaccine system</Label>
            <Input
              id="vaccineSystem"
              v-model="vaccineSystem"
              placeholder="e.g., http://hl7.org/fhir/sid/cvx"
              :class="invalidFieldClass(showValidation && !!vaccineSystemError)"
            />
            <p v-if="showValidation && vaccineSystemError" class="text-xs text-red-600">{{ vaccineSystemError }}</p>
          </div>
          <div class="space-y-2">
            <Label for-id="vaccineCode">Vaccine code</Label>
            <Input
              id="vaccineCode"
              v-model="vaccineCode"
              placeholder="e.g., 03"
              :class="invalidFieldClass(showValidation && !!vaccineCodeError)"
            />
            <p v-if="showValidation && vaccineCodeError" class="text-xs text-red-600">{{ vaccineCodeError }}</p>
          </div>
        </div>

        <div class="space-y-2">
          <Label for-id="vaccine-name">Vaccine display name</Label>
          <Input id="vaccine-name" v-model="vaccineDisplay" placeholder="e.g., MMR" />
        </div>

        <div class="grid grid-cols-2 gap-4">
          <div class="space-y-2">
            <Label for-id="vaccination-date">Occurrence date</Label>
            <div class="relative">
              <CalendarIcon class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
              <input
                id="vaccination-date"
                type="date"
                v-model="date"
                :class="[
                  'border-input bg-input-background dark:bg-input/30 flex h-9 w-full rounded-md border pl-10 pr-3 text-sm outline-none focus-visible:ring-[3px] focus-visible:ring-ring/50',
                  invalidFieldClass(showValidation && !!dateError),
                ]"
              />
            </div>
            <p v-if="showValidation && dateError" class="text-xs text-red-600">{{ dateError }}</p>
          </div>
        </div>

        <div v-if="error" class="rounded-md border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">
          {{ error }}
        </div>

        <div class="flex justify-end gap-2 pt-4">
          <Button variant="outline" type="button" @click="emit('update:open', false)" :disabled="loading">Cancel</Button>
          <Button type="submit" :disabled="loading || !canSubmit">{{ loading ? 'Saving…' : 'Save' }}</Button>
        </div>
      </form>
    </DialogContent>
  </Dialog>
</template>

<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { Calendar as CalendarIcon } from 'lucide-vue-next'

import { backendFetch } from '@/api/backend'

import Dialog from '@/components/ui/Dialog.vue'
import DialogContent from '@/components/ui/DialogContent.vue'
import DialogHeader from '@/components/ui/DialogHeader.vue'
import DialogTitle from '@/components/ui/DialogTitle.vue'
import DialogDescription from '@/components/ui/DialogDescription.vue'
import Button from '@/components/ui/Button.vue'
import Input from '@/components/ui/Input.vue'
import Label from '@/components/ui/Label.vue'

import { vaccineCatalog } from '@/vaccines/vaccineCatalog'
import { invalidFieldClass, isBlank, isFutureDate, isValidDateInput, toFriendlyFormError } from '@/utils/formFeedback'

const props = defineProps<{ patientId: string; doctorId: string; open: boolean }>()
const emit = defineEmits<{
  (e: 'update:open', v: boolean): void
  (e: 'saved'): void
}>()

const date = ref(new Date().toISOString().slice(0, 10))
const selectedCatalog = ref<string>('')
const vaccineSystem = ref('http://hl7.org/fhir/sid/cvx')
const vaccineCode = ref('')
const vaccineDisplay = ref('')
const loading = ref(false)
const error = ref<string | null>(null)
const submitAttempted = ref(false)

const catalogOptions = computed(() => {
  const map = new Map<string, { value: string; label: string; system: string; code: string; display: string }>()
  for (const v of vaccineCatalog) {
    for (const c of v.codings ?? []) {
      const value = `${c.system}|${c.code}`
      if (map.has(value)) continue
      const label = c.display ? `${v.label} — ${c.display} (CVX ${c.code})` : `${v.label} (CVX ${c.code})`
      map.set(value, { value, label, system: c.system, code: c.code, display: c.display ?? v.label })
    }
  }
  return Array.from(map.values()).sort((a, b) => a.label.localeCompare(b.label))
})

const vaccineSystemError = computed(() => {
  if (isBlank(vaccineSystem.value)) return 'Choose or enter a coding system.'
  return ''
})

const vaccineCodeError = computed(() => {
  if (isBlank(vaccineCode.value)) return 'Enter a vaccine code.'
  return ''
})

const dateError = computed(() => {
  if (isBlank(date.value)) return 'Choose the vaccination date.'
  if (!isValidDateInput(date.value)) return 'Enter a valid date.'
  if (isFutureDate(date.value)) return 'Vaccination dates cannot be in the future.'
  return ''
})

const canSubmit = computed(() => !vaccineSystemError.value && !vaccineCodeError.value && !dateError.value)
const showValidation = computed(() => submitAttempted.value)

watch(
  () => selectedCatalog.value,
  (v) => {
    if (!v) return
    if (v === 'custom') {
      vaccineSystem.value = 'http://example.org/vax-registry/vaccine'
      vaccineCode.value = ''
      return
    }
    const opt = catalogOptions.value.find((o) => o.value === v)
    if (!opt) return
    vaccineSystem.value = opt.system
    vaccineCode.value = opt.code
    vaccineDisplay.value = opt.display
  },
)

watch(
  () => props.open,
  (v) => {
    if (!v) return
    date.value = new Date().toISOString().slice(0, 10)
    selectedCatalog.value = ''
    vaccineSystem.value = 'http://hl7.org/fhir/sid/cvx'
    vaccineCode.value = ''
    vaccineDisplay.value = ''
    error.value = null
    submitAttempted.value = false
  },
)

async function handleSubmit() {
  submitAttempted.value = true
  error.value = null

  if (!canSubmit.value) {
    error.value = 'Please complete the required vaccination details before saving.'
    return
  }

  loading.value = true
  try {
    await backendFetch(`/api/practitioner/patients/${encodeURIComponent(props.patientId)}/create-immunization`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({
        vaccineSystem: vaccineSystem.value.trim() || undefined,
        vaccineCode: vaccineCode.value.trim(),
        vaccineDisplay: vaccineDisplay.value.trim() || undefined,
        date: date.value,
        performerId: props.doctorId || undefined,
      }),
    })

    emit('update:open', false)
    emit('saved')
  } catch (e) {
    error.value = toFriendlyFormError(e, 'Saving the vaccination')
  } finally {
    loading.value = false
  }
}
</script>
